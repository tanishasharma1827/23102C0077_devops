package com.sportsbooking.controller;

import com.sportsbooking.model.Booking;
import com.sportsbooking.model.User;
import com.sportsbooking.service.BookingException;
import com.sportsbooking.service.BookingService;
import com.sportsbooking.service.DashboardService;
import com.sportsbooking.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Player side: booking request, cancellation and the player dashboard (US3, US5, US6). */
@Controller
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final DashboardService dashboardService;

    public BookingController(BookingService bookingService, UserService userService,
                             DashboardService dashboardService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.dashboardService = dashboardService;
    }

    private User signedIn(HttpSession session) {
        return userService.findById((Long) session.getAttribute(UserController.SESSION_USER)).orElse(null);
    }

    @PostMapping("/bookings")
    public String request(@RequestParam Long slotId, @RequestParam Long facilityId, @RequestParam String date,
                          HttpSession session, RedirectAttributes redirect) {
        User currentUser = signedIn(session);
        if (currentUser == null) {
            redirect.addFlashAttribute("error", "Please register or sign in to book a slot.");
            return "redirect:/signin";
        }
        try {
            Booking booking = bookingService.request(currentUser, slotId);
            redirect.addFlashAttribute("message", "Booking requested for " + booking.getSlot().getFacility().getName()
                    + " on " + booking.getSlot().getDate() + " at " + booking.getSlot().getLabel()
                    + ". Status: REQUESTED");
        } catch (BookingException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/slots?facilityId=" + facilityId + "&date=" + date;
    }

    @GetMapping("/bookings/my")
    public String myBookings(@RequestParam(required = false) String tab, HttpSession session, Model model,
                             RedirectAttributes redirect) {
        User currentUser = signedIn(session);
        if (currentUser == null) {
            redirect.addFlashAttribute("error", "Please sign in to see your bookings.");
            return "redirect:/signin";
        }
        DashboardService.PlayerDashboard dashboard = dashboardService.playerDashboard(currentUser);
        String view = "history".equals(tab) ? "history" : "upcoming";
        model.addAttribute("dash", dashboard);
        if (dashboard.nextGame() != null) {
            model.addAttribute("daysToNext", java.time.temporal.ChronoUnit.DAYS.between(
                    java.time.LocalDate.now(), dashboard.nextGame().getSlot().getDate()));
        }
        model.addAttribute("tab", view);
        model.addAttribute("bookings", view.equals("history") ? dashboard.history() : dashboard.upcoming());
        return "my-bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession session,
                         RedirectAttributes redirect) {
        User currentUser = signedIn(session);
        if (currentUser == null) {
            return "redirect:/signin";
        }
        try {
            bookingService.cancel(currentUser, id);
            redirect.addFlashAttribute("message", "Booking #" + id + " cancelled. The slot is free again.");
        } catch (BookingException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings/my";
    }
}
