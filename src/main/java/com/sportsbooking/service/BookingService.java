package com.sportsbooking.service;

import com.sportsbooking.model.*;
import com.sportsbooking.repository.BookingRepository;
import com.sportsbooking.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {

    /** A slot is taken while it has a booking in one of these states. */
    static final Set<BookingStatus> ACTIVE = Set.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;

    public BookingService(BookingRepository bookingRepository, SlotRepository slotRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
    }

    /** US3 + M10: create a REQUESTED booking, refusing a slot that is already taken. */
    @Transactional
    public synchronized Booking request(User user, Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new BookingException("Slot not found"));
        if (bookingRepository.existsBySlotIdAndStatusIn(slotId, ACTIVE)) {
            throw new BookingException("Sorry, this slot is already booked. Please choose another slot.");
        }
        return bookingRepository.save(new Booking(user, slot));
    }

    /** US4: manager confirms a requested booking. */
    @Transactional
    public Booking confirm(User manager, Long bookingId) {
        if (manager.getRole() != Role.MANAGER) {
            throw new BookingException("Only a facility manager can confirm bookings");
        }
        Booking booking = get(bookingId);
        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new BookingException("Only REQUESTED bookings can be confirmed");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return booking;
    }

    /** US5: the player who made the booking cancels it; the slot becomes free again. */
    @Transactional
    public Booking cancel(User user, Long bookingId) {
        Booking booking = get(bookingId);
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BookingException("You can only cancel your own bookings");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("This booking is already cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return booking;
    }

    /** US6 */
    public List<Booking> bookingsOf(User user) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public List<Booking> pendingRequests() {
        return bookingRepository.findByStatusOrderByCreatedAt(BookingStatus.REQUESTED);
    }

    /** Ids of the given slots that are currently taken (used to mark slots as Booked). */
    public Set<Long> takenSlotIds(List<Slot> slots) {
        List<Long> ids = slots.stream().map(Slot::getId).toList();
        return bookingRepository.findBySlotIdInAndStatusIn(ids, ACTIVE).stream()
                .map(b -> b.getSlot().getId())
                .collect(Collectors.toSet());
    }

    private Booking get(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Booking not found"));
    }
}
