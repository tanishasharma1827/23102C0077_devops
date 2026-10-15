package com.sportsbooking.controller;

import com.sportsbooking.model.Slot;
import com.sportsbooking.service.BookingService;
import com.sportsbooking.service.SlotService;
import com.sportsbooking.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Facility list and slot availability (US2). */
@Controller
public class SlotController {

    private final SlotService slotService;
    private final BookingService bookingService;
    private final StatsService statsService;

    public SlotController(SlotService slotService, BookingService bookingService, StatsService statsService) {
        this.slotService = slotService;
        this.bookingService = bookingService;
        this.statsService = statsService;
    }

    @GetMapping("/facilities")
    public String facilities(Model model) {
        model.addAttribute("facilities", slotService.allFacilities());
        model.addAttribute("freeToday", statsService.freeSlotsToday());
        return "facilities";
    }

    @GetMapping("/slots")
    public String slots(@RequestParam Long facilityId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        Model model) {
        LocalDate today = LocalDate.now();
        LocalDate day = date != null ? date : today;
        model.addAttribute("facility", slotService.getFacility(facilityId));
        model.addAttribute("date", day);
        model.addAttribute("today", today);
        // A date before today can never be booked: the page shows a "not possible" banner instead of Book buttons
        model.addAttribute("pastDate", day.isBefore(today));
        model.addAttribute("week", today.datesUntil(today.plusDays(7)).toList());
        List<Slot> slots = slotService.availableSlots(facilityId, day);
        Set<Long> taken = bookingService.takenSlotIds(slots);
        model.addAttribute("slots", slots);
        model.addAttribute("takenSlotIds", taken);
        model.addAttribute("freeCount", slots.stream().filter(s -> !s.isPast() && !taken.contains(s.getId())).count());
        return "slots";
    }
}
