package com.sportsbooking.controller;

import com.sportsbooking.service.SlotService;
import com.sportsbooking.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the home page and the Insights (stats) dashboard. */
@Controller
public class HomeController {

    private final SlotService slotService;
    private final StatsService statsService;

    public HomeController(SlotService slotService, StatsService statsService) {
        this.slotService = slotService;
        this.statsService = statsService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appName", "Sports Facility Booking System");
        model.addAttribute("facilities", slotService.allFacilities());
        model.addAttribute("freeToday", statsService.freeSlotsToday());
        model.addAttribute("stats", statsService.dashboard());
        return "index";
    }

    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("stats", statsService.dashboard());
        model.addAttribute("historyDays", StatsService.HISTORY_DAYS);
        return "stats";
    }
}
