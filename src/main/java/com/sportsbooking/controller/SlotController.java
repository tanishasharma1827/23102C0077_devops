package com.sportsbooking.controller;

import com.sportsbooking.model.Slot;
import com.sportsbooking.service.BookingService;
import com.sportsbooking.service.SlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/** Facility list and slot availability (US2). */
@Controller
public class SlotController {

    private final SlotService slotService;
    private final BookingService bookingService;

    public SlotController(SlotService slotService, BookingService bookingService) {
        this.slotService = slotService;
        this.bookingService = bookingService;
    }

    @GetMapping("/facilities")
    public String facilities(Model model) {
        model.addAttribute("facilities", slotService.allFacilities());
        return "facilities";
    }

    @GetMapping("/slots")
    public String slots(@RequestParam Long facilityId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        Model model) {
        LocalDate day = date != null ? date : LocalDate.now();
        model.addAttribute("facility", slotService.getFacility(facilityId));
        model.addAttribute("date", day);
        List<Slot> slots = slotService.availableSlots(facilityId, day);
        model.addAttribute("slots", slots);
        model.addAttribute("takenSlotIds", bookingService.takenSlotIds(slots));
        return "slots";
    }
}
