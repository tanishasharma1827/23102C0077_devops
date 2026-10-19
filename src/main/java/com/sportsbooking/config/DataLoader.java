package com.sportsbooking.config;

import com.sportsbooking.model.*;
import com.sportsbooking.repository.BookingRepository;
import com.sportsbooking.repository.FacilityRepository;
import com.sportsbooking.repository.SlotRepository;
import com.sportsbooking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Loads sample facilities, a manager account and slots from 14 days ago to 6 days ahead (M6).
 * Past days get a realistic booking history, so the stats dashboard has data to show.
 */
@Component
public class DataLoader implements CommandLineRunner {

    public static final String MANAGER_EMAIL = "manager@sports.local";
    static final int[] START_HOURS = {6, 7, 8, 16, 17, 18, 19, 20};
    static final int PAST_DAYS = 14;
    static final int FUTURE_DAYS = 7;

    private final FacilityRepository facilityRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public DataLoader(FacilityRepository facilityRepository, SlotRepository slotRepository,
                      UserRepository userRepository, BookingRepository bookingRepository) {
        this.facilityRepository = facilityRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void run(String... args) {
        if (facilityRepository.count() > 0) {
            return;
        }
        List<Facility> facilities = facilityRepository.saveAll(List.of(
                new Facility("Badminton Court 1", "BADMINTON", "Indoor Hall"),
                new Facility("Football Turf", "FOOTBALL", "Main Ground"),
                new Facility("Table Tennis Table 1", "TABLE_TENNIS", "Recreation Room"),
                new Facility("Tennis Court", "TENNIS", "East Wing"),
                new Facility("Basketball Court", "BASKETBALL", "Open Air Arena"),
                new Facility("Cricket Nets", "CRICKET", "Practice Ground")));

        userRepository.save(new User("Facility Manager", MANAGER_EMAIL, "9999999999", Role.MANAGER));
        List<User> players = userRepository.saveAll(List.of(
                new User("Aarav Mehta", "aarav@demo.local", "9810000001", Role.PLAYER),
                new User("Diya Patel", "diya@demo.local", "9810000002", Role.PLAYER),
                new User("Kabir Singh", "kabir@demo.local", "9810000003", Role.PLAYER),
                new User("Isha Nair", "isha@demo.local", "9810000004", Role.PLAYER),
                new User("Rohan Gupta", "rohan@demo.local", "9810000005", Role.PLAYER),
                new User("Sneha Iyer", "sneha@demo.local", "9810000006", Role.PLAYER),
                new User("Arjun Rao", "arjun@demo.local", "9810000007", Role.PLAYER),
                new User("Meera Joshi", "meera@demo.local", "9810000008", Role.PLAYER)));

        LocalDate today = LocalDate.now();
        List<Slot> slots = new ArrayList<>();
        for (Facility facility : facilities) {
            for (int day = -PAST_DAYS; day < FUTURE_DAYS; day++) {
                for (int hour : START_HOURS) {
                    slots.add(new Slot(facility, today.plusDays(day), LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0)));
                }
            }
        }
        slots = slotRepository.saveAll(slots);

        // Booking history: evenings are busier than mornings, football and badminton are the most popular.
        Random random = new Random(2026);
        List<Booking> history = new ArrayList<>();
        for (Slot slot : slots) {
            if (!slot.getDate().isBefore(today)) {
                continue;
            }
            int hour = slot.getStartTime().getHour();
            double chance = (hour >= 17 ? 0.70 : 0.35) + popularity(slot.getFacility().getType());
            if (random.nextDouble() < chance) {
                double roll = random.nextDouble();
                BookingStatus status = roll < 0.10 ? BookingStatus.CANCELLED
                        : roll < 0.14 ? BookingStatus.REJECTED : BookingStatus.CONFIRMED;
                User player = players.get(random.nextInt(players.size()));
                history.add(new Booking(player, slot, status,
                        slot.getStart().minusDays(1 + random.nextInt(3)).minusMinutes(random.nextInt(600))));
            }
        }
        bookingRepository.saveAll(history);

        // A few upcoming bookings on the last three facilities, so the manager queue and the
        // player dashboard are not empty in a demo. Courts 1-3 stay free for the Selenium tests.
        List<Booking> upcoming = new ArrayList<>();
        int n = 0;
        for (Slot slot : slots) {
            boolean demoFacility = slot.getFacility().getId() > facilities.get(2).getId();
            boolean soon = !slot.isPast() && !slot.getDate().isAfter(today.plusDays(3));
            if (demoFacility && soon && slot.getStartTime().getHour() >= 17 && random.nextDouble() < 0.35) {
                BookingStatus status = n % 3 == 0 ? BookingStatus.REQUESTED : BookingStatus.CONFIRMED;
                upcoming.add(new Booking(players.get(n % players.size()), slot, status,
                        java.time.LocalDateTime.now().minusHours(20 - n).minusMinutes(7L * n)));
                n++;
            }
        }
        bookingRepository.saveAll(upcoming);
    }

    private static double popularity(String type) {
        return switch (type) {
            case "FOOTBALL", "BADMINTON" -> 0.15;
            case "CRICKET", "BASKETBALL" -> 0.0;
            default -> -0.10;
        };
    }
}
