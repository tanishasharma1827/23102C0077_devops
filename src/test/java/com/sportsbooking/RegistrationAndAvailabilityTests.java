package com.sportsbooking;

import com.sportsbooking.repository.FacilityRepository;
import com.sportsbooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RegistrationAndAvailabilityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Test
    void registersNewPlayerAndRedirectsToFacilities() throws Exception {
        mockMvc.perform(post("/register")
                        .param("name", "Asha Patil")
                        .param("email", "asha@example.com")
                        .param("phone", "9876543210"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/facilities"));

        assertThat(userRepository.existsByEmailIgnoreCase("asha@example.com")).isTrue();
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/register")
                    .param("name", "Asha Patil")
                    .param("email", "dup@example.com")
                    .param("phone", "9876543210"));
        }
        mockMvc.perform(post("/register")
                        .param("name", "Other")
                        .param("email", "DUP@example.com")
                        .param("phone", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("already exists")));
    }

    @Test
    void rejectsInvalidPhone() throws Exception {
        mockMvc.perform(post("/register")
                        .param("name", "Asha")
                        .param("email", "asha2@example.com")
                        .param("phone", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Phone must be 10 digits")));
    }

    @Test
    void listsSeededFacilities() throws Exception {
        mockMvc.perform(get("/facilities"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Badminton Court 1")))
                .andExpect(content().string(containsString("Football Turf")));
    }

    @Test
    void showsSlotsForTodayAndNoneForFarFuture() throws Exception {
        Long id = facilityRepository.findAll().get(0).getId();
        mockMvc.perform(get("/slots").param("facilityId", id.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("06:00 - 07:00")));

        mockMvc.perform(get("/slots").param("facilityId", id.toString())
                        .param("date", LocalDate.now().plusYears(1).toString()))
                .andExpect(content().string(containsString("No slots available")));
    }

    @Test
    void pastDateShowsBookingNotPossible() throws Exception {
        Long id = facilityRepository.findAll().get(0).getId();
        mockMvc.perform(get("/slots").param("facilityId", id.toString())
                        .param("date", LocalDate.now().minusDays(14).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Booking not possible for a past date")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("id=\"book-"))));
    }

    @Test
    void statsDashboardShowsCharts() throws Exception {
        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Booking statistics")))
                .andExpect(content().string(containsString("chart-daily")));
    }
}
