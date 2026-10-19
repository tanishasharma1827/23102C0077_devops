package com.sportsbooking;

import com.sportsbooking.config.DataLoader;
import com.sportsbooking.controller.UserController;
import com.sportsbooking.model.*;
import com.sportsbooking.repository.SlotRepository;
import com.sportsbooking.repository.UserRepository;
import com.sportsbooking.service.BookingException;
import com.sportsbooking.service.BookingService;
import com.sportsbooking.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingFlowTests {

    @Autowired private BookingService bookingService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private SlotRepository slotRepository;
    @Autowired private MockMvc mockMvc;

    private User asha;
    private User ravi;
    private User manager;
    private Slot slot;

    @BeforeEach
    void setUp() {
        asha = userService.register(new User("Asha", "asha@example.com", "9876543210", Role.PLAYER));
        ravi = userService.register(new User("Ravi", "ravi@example.com", "9876543211", Role.PLAYER));
        manager = userRepository.findByEmailIgnoreCase(DataLoader.MANAGER_EMAIL).orElseThrow();
        slot = freeFutureSlots().get(0);
    }

    /** Free slots from tomorrow onwards (the demo data also has past slots with booking history). */
    private List<Slot> freeFutureSlots() {
        return slotRepository.findAll().stream()
                .filter(s -> s.getDate().isAfter(LocalDate.now()))
                .filter(s -> bookingService.takenSlotIds(List.of(s)).isEmpty())
                .toList();
    }

    @Test
    void newBookingIsRequestedAndSlotBecomesTaken() {
        Booking booking = bookingService.request(asha, slot.getId());

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REQUESTED);
        assertThat(bookingService.takenSlotIds(List.of(slot))).containsExactly(slot.getId());
    }

    @Test
    void doubleBookingIsBlocked() {
        bookingService.request(asha, slot.getId());

        assertThatThrownBy(() -> bookingService.request(ravi, slot.getId()))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void pastSlotCannotBeBooked() {
        Slot past = slotRepository.findAll().stream()
                .filter(s -> s.getDate().isBefore(LocalDate.now()))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> bookingService.request(asha, past.getId()))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("Booking not possible");
    }

    @Test
    void managerQueueIsFirstComeFirstServed() {
        Booking first = bookingService.request(ravi, freeFutureSlots().get(1).getId());
        Booking second = bookingService.request(asha, freeFutureSlots().get(0).getId());

        assertThat(bookingService.pendingRequests()).extracting(Booking::getId)
                .containsSubsequence(first.getId(), second.getId());
    }

    @Test
    void managerConfirmsRequest() {
        Booking booking = bookingService.request(asha, slot.getId());

        assertThat(bookingService.confirm(manager, booking.getId()).getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void playerCannotConfirm() {
        Booking booking = bookingService.request(asha, slot.getId());

        assertThatThrownBy(() -> bookingService.confirm(ravi, booking.getId()))
                .isInstanceOf(BookingException.class);
    }

    @Test
    void cancellingFreesTheSlot() {
        Booking booking = bookingService.request(asha, slot.getId());
        bookingService.cancel(asha, booking.getId());

        assertThat(bookingService.takenSlotIds(List.of(slot))).isEmpty();
        assertThat(bookingService.request(ravi, slot.getId()).getStatus()).isEqualTo(BookingStatus.REQUESTED);
    }

    @Test
    void playerCannotCancelSomeoneElsesBooking() {
        Booking booking = bookingService.request(asha, slot.getId());

        assertThatThrownBy(() -> bookingService.cancel(ravi, booking.getId()))
                .hasMessageContaining("your own");
    }

    @Test
    void webFlowBookThenSeeStatusInMyBookings() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserController.SESSION_USER, asha.getId());

        mockMvc.perform(post("/bookings").session(session)
                        .param("slotId", slot.getId().toString())
                        .param("facilityId", slot.getFacility().getId().toString())
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/bookings/my").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("REQUESTED")));
    }

    @Test
    void managerRejectsRequestAndSlotIsFreeAgain() {
        Booking booking = bookingService.request(asha, slot.getId());

        assertThat(bookingService.reject(manager, booking.getId()).getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(bookingService.takenSlotIds(List.of(slot))).isEmpty();
        assertThatThrownBy(() -> bookingService.reject(ravi, booking.getId()))
                .hasMessageContaining("Only a facility manager");
    }

    @Test
    void managerCanCancelAConfirmedBooking() {
        Booking booking = bookingService.request(asha, slot.getId());
        bookingService.confirm(manager, booking.getId());

        assertThat(bookingService.cancelByManager(manager, booking.getId()).getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(bookingService.takenSlotIds(List.of(slot))).isEmpty();
    }

    @Test
    void managerDashboardTabsRender() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserController.SESSION_USER, manager.getId());
        bookingService.request(asha, slot.getId());

        mockMvc.perform(get("/manager/bookings").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("requests-table")))
                .andExpect(content().string(containsString("Asha")));
        mockMvc.perform(get("/manager/bookings").param("tab", "schedule").session(session))
                .andExpect(content().string(containsString("schedule-grid")));
        mockMvc.perform(get("/manager/bookings").param("tab", "all").param("q", "asha").session(session))
                .andExpect(content().string(containsString("all-bookings-table")))
                .andExpect(content().string(containsString("1 booking(s) found")));
        mockMvc.perform(get("/manager/bookings").param("tab", "players").session(session))
                .andExpect(content().string(containsString("players-table")));
    }

    @Test
    void playerDashboardShowsNextGameAndTabs() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserController.SESSION_USER, asha.getId());
        bookingService.request(asha, slot.getId());

        mockMvc.perform(get("/bookings/my").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your next game")))
                .andExpect(content().string(containsString("bookings-table")));
        mockMvc.perform(get("/bookings/my").param("tab", "history").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No past games yet")));
    }

    @Test
    void managerPageIsProtected() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserController.SESSION_USER, asha.getId());

        mockMvc.perform(get("/manager/bookings").session(session))
                .andExpect(redirectedUrl("/signin"));
    }
}
