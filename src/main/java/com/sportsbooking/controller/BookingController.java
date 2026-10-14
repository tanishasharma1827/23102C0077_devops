package com.sportsbooking.controller;

import com.sportsbooking.model.Booking;
import com.sportsbooking.model.Role;
import com.sportsbooking.model.User;
import com.sportsbooking.service.BookingException;
import com.sportsbooking.service.BookingService;
import com.sportsbooking.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Booking request, confirmation, cancellation and status tracking (US3-US6). */
@Controller
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    public BookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
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
    public String myBookings(HttpSession session, Model model,
                             RedirectAttributes redirect) {
        User currentUser = signedIn(session);
        if (currentUser == null) {
            redirect.addFlashAttribute("error", "Please sign in to see your bookings.");
            return "redirect:/signin";
        }
        model.addAttribute("bookings", bookingService.bookingsOf(currentUser));
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

    @GetMapping("/manager/bookings")
    public String managerBookings(HttpSession session, Model model,
                                  RedirectAttributes redirect) {
        User currentUser = signedIn(session);
        if (currentUser == null || currentUser.getRole() != Role.MANAGER) {
            redirect.addFlashAttribute("error", "Manager access only. Sign in as the facility manager.");
            return "redirect:/signin";
        }
        model.addAttribute("bookings", bookingService.pendingRequests());
        return "manager-bookings";
    }

    @PostMapping("/bookings/{id}/confirm")
    public String confirm(@PathVariable Long id, HttpSession session,
                          RedirectAttributes redirect) {
        User currentUser = signedIn(session);
        if (currentUser == null) {
            return "redirect:/signin";
        }
        try {
            bookingService.confirm(currentUser, id);
            redirect.addFlashAttribute("message", "Booking #" + id + " confirmed.");
        } catch (BookingException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/bookings";
    }
}
