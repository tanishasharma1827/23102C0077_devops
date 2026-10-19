package com.sportsbooking.controller;

import com.sportsbooking.model.BookingStatus;
import com.sportsbooking.model.Role;
import com.sportsbooking.model.User;
import com.sportsbooking.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Manager dashboard (US4) with four tabs: Requests (FCFS queue), Schedule, All bookings and Players.
 */
@Controller
public class ManagerController {

    static final List<String> TABS = List.of("requests", "schedule", "all", "players");
    static final int PAGE_SIZE = 25;

    private final BookingService bookingService;
    private final DashboardService dashboardService;
    private final SlotService slotService;
    private final UserService userService;

    public ManagerController(BookingService bookingService, DashboardService dashboardService,
                             SlotService slotService, UserService userService) {
        this.bookingService = bookingService;
        this.dashboardService = dashboardService;
        this.slotService = slotService;
        this.userService = userService;
    }

    private User manager(HttpSession session) {
        User user = userService.findById((Long) session.getAttribute(UserController.SESSION_USER)).orElse(null);
        return user != null && user.getRole() == Role.MANAGER ? user : null;
    }

    @GetMapping("/manager/bookings")
    public String dashboard(@RequestParam(defaultValue = "requests") String tab,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) Long facilityId,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            @RequestParam(required = false) String q,
                            @RequestParam(defaultValue = "1") int page,
                            HttpServletRequest request, HttpSession session, Model model,
                            RedirectAttributes redirect) {
        if (manager(session) == null) {
            redirect.addFlashAttribute("error", "Manager access only. Sign in as the facility manager.");
            return "redirect:/signin";
        }
        String view = TABS.contains(tab) ? tab : "requests";
        model.addAttribute("tab", view);
        model.addAttribute("back", "?" + (request.getQueryString() != null ? request.getQueryString() : "tab=" + view));
        model.addAttribute("summary", dashboardService.managerSummary());
        model.addAttribute("bookings", bookingService.pendingRequests());   // Requests tab + badge count
        model.addAttribute("today", LocalDate.now());
        switch (view) {
            case "schedule" -> model.addAttribute("schedule",
                    dashboardService.schedule(date != null ? date : LocalDate.now()));
            case "all" -> {
                List<com.sportsbooking.model.Booking> found = dashboardService.searchBookings(status, facilityId, date, q);
                int pages = Math.max(1, (found.size() + PAGE_SIZE - 1) / PAGE_SIZE);
                int current = Math.min(Math.max(page, 1), pages);
                model.addAttribute("results", found.subList((current - 1) * PAGE_SIZE,
                        Math.min(current * PAGE_SIZE, found.size())));
                model.addAttribute("total", found.size());
                model.addAttribute("page", current);
                model.addAttribute("pages", pages);
                model.addAttribute("facilities", slotService.allFacilities());
                model.addAttribute("statuses", BookingStatus.values());
                model.addAttribute("fStatus", status);
                model.addAttribute("fFacility", facilityId);
                model.addAttribute("fDate", date);
                model.addAttribute("fQuery", q);
            }
            case "players" -> model.addAttribute("players", dashboardService.players());
            default -> { }
        }
        return "manager-bookings";
    }

    @PostMapping("/bookings/{id}/confirm")
    public String confirm(@PathVariable Long id, @RequestParam(required = false) String back,
                          HttpSession session, RedirectAttributes redirect) {
        return act(session, redirect, back, () -> bookingService.confirm(manager(session), id),
                "Booking #" + id + " confirmed.");
    }

    @PostMapping("/bookings/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam(required = false) String back,
                         HttpSession session, RedirectAttributes redirect) {
        return act(session, redirect, back, () -> bookingService.reject(manager(session), id),
                "Booking #" + id + " rejected. The slot is free again.");
    }

    @PostMapping("/manager/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id, @RequestParam(required = false) String back,
                         HttpSession session, RedirectAttributes redirect) {
        return act(session, redirect, back, () -> bookingService.cancelByManager(manager(session), id),
                "Booking #" + id + " cancelled by the manager. The slot is free again.");
    }

    private String act(HttpSession session, RedirectAttributes redirect, String back, Runnable action, String message) {
        if (manager(session) == null) {
            return "redirect:/signin";
        }
        try {
            action.run();
            redirect.addFlashAttribute("message", message);
        } catch (BookingException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        // "back" keeps the manager on the same tab and filters; only a query string is accepted
        return "redirect:/manager/bookings" + (back != null && back.startsWith("?") ? back : "");
    }
}
