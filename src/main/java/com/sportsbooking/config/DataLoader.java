package com.sportsbooking.config;

import com.sportsbooking.model.Facility;
import com.sportsbooking.model.Role;
import com.sportsbooking.model.Slot;
import com.sportsbooking.model.User;
import com.sportsbooking.repository.FacilityRepository;
import com.sportsbooking.repository.SlotRepository;
import com.sportsbooking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Loads sample facilities, slots for the next 7 days and a manager account (M6).
 */
@Component
public class DataLoader implements CommandLineRunner {

    public static final String MANAGER_EMAIL = "manager@sports.local";
    private static final int[] START_HOURS = {6, 7, 17, 18, 19, 20};

    private final FacilityRepository facilityRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;

    public DataLoader(FacilityRepository facilityRepository, SlotRepository slotRepository,
                      UserRepository userRepository) {
        this.facilityRepository = facilityRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (facilityRepository.count() > 0) {
            return;
        }
        List<Facility> facilities = facilityRepository.saveAll(List.of(
                new Facility("Badminton Court 1", "BADMINTON", "Indoor Hall"),
                new Facility("Football Turf", "FOOTBALL", "Main Ground"),
                new Facility("Table Tennis Table 1", "TABLE_TENNIS", "Recreation Room")));

        LocalDate today = LocalDate.now();
        for (Facility facility : facilities) {
            for (int day = 0; day < 7; day++) {
                for (int hour : START_HOURS) {
                    slotRepository.save(new Slot(facility, today.plusDays(day),
                            LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0)));
                }
            }
        }

        userRepository.save(new User("Facility Manager", MANAGER_EMAIL, "9999999999", Role.MANAGER));
    }
}
