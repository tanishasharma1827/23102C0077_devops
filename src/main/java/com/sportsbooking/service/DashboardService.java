package com.sportsbooking.service;

import com.sportsbooking.model.*;
import com.sportsbooking.repository.BookingRepository;
import com.sportsbooking.repository.FacilityRepository;
import com.sportsbooking.repository.SlotRepository;
import com.sportsbooking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Builds what the manager dashboard and the player dashboard show. */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    public record ManagerSummary(long pending, long todayBookings, long upcomingConfirmed, int todayUsage) { }

    public record ScheduleCell(Slot slot, Booking booking) {
        public boolean isPast() { return slot.isPast(); }
    }

    public record ScheduleRow(Facility facility, List<ScheduleCell> cells) { }

    public record Schedule(LocalDate date, List<String> hours, List<ScheduleRow> rows, long booked, long free) { }

    public record PlayerStat(User user, long total, long confirmed, long cancelled, LocalDate lastGame) { }

    public record PlayerDashboard(List<Booking> upcoming, List<Booking> history, Booking nextGame,
                                  long gamesPlayed, long upcomingActive, long cancelled, String favouriteSport) { }

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;

    public DashboardService(BookingService bookingService, BookingRepository bookingRepository,
                            SlotRepository slotRepository, FacilityRepository facilityRepository,
                            UserRepository userRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
    }

    // ---------------- manager ----------------

    public ManagerSummary managerSummary() {
        LocalDate today = LocalDate.now();
        List<Booking> all = bookingRepository.findAll();
        long todayActive = all.stream().filter(b -> b.isActive() && b.getSlot().getDate().equals(today)).count();
        long upcomingConfirmed = all.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED && !b.getSlot().isPast()).count();
        long slotsToday = facilityRepository.findAll().stream()
                .mapToLong(f -> slotRepository.findByFacilityIdAndDateOrderByStartTime(f.getId(), today).size()).sum();
        int usage = slotsToday == 0 ? 0 : (int) Math.round(100.0 * todayActive / slotsToday);
        return new ManagerSummary(bookingService.pendingRequests().size(), todayActive, upcomingConfirmed, usage);
    }

    /** Facility x time grid for one day: who has booked which slot. */
    public Schedule schedule(LocalDate date) {
        Map<Long, Booking> booked = bookingService.activeBookingsOn(date);
        List<ScheduleRow> rows = new ArrayList<>();
        SortedSet<String> hours = new TreeSet<>();
        for (Facility f : facilityRepository.findAll()) {
            List<ScheduleCell> cells = slotRepository.findByFacilityIdAndDateOrderByStartTime(f.getId(), date).stream()
                    .map(s -> new ScheduleCell(s, booked.get(s.getId())))
                    .toList();
            cells.forEach(c -> hours.add(c.slot().getStartTime().toString()));
            rows.add(new ScheduleRow(f, cells));
        }
        long total = rows.stream().mapToLong(r -> r.cells().size()).sum();
        return new Schedule(date, List.copyOf(hours), rows, booked.size(), total - booked.size());
    }

    /** All bookings, filtered by status, facility, slot date and player name/email. */
    public List<Booking> searchBookings(String status, Long facilityId, LocalDate date, String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return bookingService.allBookings().stream()
                .filter(b -> status == null || status.isBlank() || b.getStatus().name().equals(status))
                .filter(b -> facilityId == null || b.getSlot().getFacility().getId().equals(facilityId))
                .filter(b -> date == null || b.getSlot().getDate().equals(date))
                .filter(b -> q.isEmpty() || b.getUser().getName().toLowerCase().contains(q)
                        || b.getUser().getEmail().toLowerCase().contains(q))
                .toList();
    }

    public List<PlayerStat> players() {
        Map<Long, List<Booking>> byUser = bookingRepository.findAll().stream()
                .collect(Collectors.groupingBy(b -> b.getUser().getId()));
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.PLAYER)
                .map(u -> {
                    List<Booking> list = byUser.getOrDefault(u.getId(), List.of());
                    return new PlayerStat(u, list.size(),
                            list.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count(),
                            list.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count(),
                            list.stream().filter(b -> b.isActive() && b.getSlot().isPast())
                                    .map(b -> b.getSlot().getDate()).max(Comparator.naturalOrder()).orElse(null));
                })
                .sorted(Comparator.comparingLong(PlayerStat::total).reversed())
                .toList();
    }

    // ---------------- player ----------------

    public PlayerDashboard playerDashboard(User user) {
        List<Booking> mine = bookingService.bookingsOf(user);
        Comparator<Booking> bySlot = Comparator.comparing(b -> b.getSlot().getStart());
        List<Booking> upcoming = mine.stream().filter(b -> !b.getSlot().isPast()).sorted(bySlot).toList();
        List<Booking> history = mine.stream().filter(b -> b.getSlot().isPast()).sorted(bySlot.reversed()).toList();
        Booking next = upcoming.stream().filter(Booking::isActive).findFirst().orElse(null);
        String favourite = mine.stream().filter(Booking::isActive)
                .collect(Collectors.groupingBy(b -> b.getSlot().getFacility().getSport(), Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("-");
        return new PlayerDashboard(upcoming, history, next,
                history.stream().filter(Booking::isActive).count(),
                upcoming.stream().filter(Booking::isActive).count(),
                mine.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count(),
                favourite);
    }
}
