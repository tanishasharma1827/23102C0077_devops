package com.sportsbooking.service;

import com.sportsbooking.model.*;
import com.sportsbooking.repository.BookingRepository;
import com.sportsbooking.repository.FacilityRepository;
import com.sportsbooking.repository.SlotRepository;
import com.sportsbooking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Numbers for the home page and the Insights dashboard. */
@Service
@Transactional(readOnly = true)
public class StatsService {

    public static final int HISTORY_DAYS = 14;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM");

    public record Point(String label, long value) { }

    public record FacilityStat(Facility facility, long bookings, long slots, int utilisation) { }

    public record Dashboard(long totalBookings, long confirmed, long requested, long cancelled, long rejected, long players,
                            int utilisation, int cancellationRate, String busiestFacility, String peakHour,
                            List<Point> daily, List<Point> byHour, List<FacilityStat> facilities) { }

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;

    public StatsService(BookingRepository bookingRepository, SlotRepository slotRepository,
                        FacilityRepository facilityRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
    }

    public Dashboard dashboard() {
        List<Booking> all = bookingRepository.findAll();
        Map<BookingStatus, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));
        List<Booking> active = all.stream().filter(Booking::isActive).toList();

        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(HISTORY_DAYS);
        List<Slot> pastSlots = slotRepository.findAll().stream()
                .filter(s -> !s.getDate().isBefore(from) && s.getDate().isBefore(today)).toList();
        List<Booking> pastActive = active.stream()
                .filter(b -> !b.getSlot().getDate().isBefore(from) && b.getSlot().getDate().isBefore(today)).toList();

        Map<LocalDate, Long> perDay = count(pastActive, b -> b.getSlot().getDate());
        List<Point> daily = new ArrayList<>();
        for (LocalDate d = from; d.isBefore(today); d = d.plusDays(1)) {
            daily.add(new Point(d.format(DAY), perDay.getOrDefault(d, 0L)));
        }

        Map<Integer, Long> perHour = count(active, b -> b.getSlot().getStartTime().getHour());
        List<Point> byHour = new TreeMap<>(perHour).entrySet().stream()
                .map(e -> new Point(String.format("%02d:00", e.getKey()), e.getValue())).toList();

        Map<Long, Long> slotsPerFacility = count(pastSlots, s -> s.getFacility().getId());
        Map<Long, Long> bookingsPerFacility = count(pastActive, b -> b.getSlot().getFacility().getId());
        List<FacilityStat> facilities = facilityRepository.findAll().stream()
                .map(f -> {
                    long booked = bookingsPerFacility.getOrDefault(f.getId(), 0L);
                    long slots = slotsPerFacility.getOrDefault(f.getId(), 0L);
                    return new FacilityStat(f, booked, slots, percent(booked, slots));
                })
                .sorted(Comparator.comparingLong(FacilityStat::bookings).reversed())
                .toList();

        long cancelled = byStatus.getOrDefault(BookingStatus.CANCELLED, 0L);
        return new Dashboard(all.size(),
                byStatus.getOrDefault(BookingStatus.CONFIRMED, 0L),
                byStatus.getOrDefault(BookingStatus.REQUESTED, 0L),
                cancelled,
                byStatus.getOrDefault(BookingStatus.REJECTED, 0L),
                userRepository.findAll().stream().filter(u -> u.getRole() == Role.PLAYER).count(),
                percent(pastActive.size(), pastSlots.size()),
                percent(cancelled, all.size()),
                facilities.isEmpty() ? "-" : facilities.get(0).facility().getName(),
                byHour.stream().max(Comparator.comparingLong(Point::value)).map(Point::label).orElse("-"),
                daily, byHour, facilities);
    }

    /** Free, bookable slots left today for every facility (facility id -> count). */
    public Map<Long, Long> freeSlotsToday() {
        LocalDate today = LocalDate.now();
        Set<Long> taken = bookingRepository.findAll().stream()
                .filter(Booking::isActive).map(b -> b.getSlot().getId()).collect(Collectors.toSet());
        Map<Long, Long> free = new HashMap<>();
        for (Facility f : facilityRepository.findAll()) {
            free.put(f.getId(), slotRepository.findByFacilityIdAndDateOrderByStartTime(f.getId(), today).stream()
                    .filter(s -> !s.isPast() && !taken.contains(s.getId())).count());
        }
        return free;
    }

    private static <T, K> Map<K, Long> count(Collection<T> items, Function<T, K> key) {
        return items.stream().collect(Collectors.groupingBy(key, Collectors.counting()));
    }

    private static int percent(long part, long whole) {
        return whole == 0 ? 0 : (int) Math.round(100.0 * part / whole);
    }
}
