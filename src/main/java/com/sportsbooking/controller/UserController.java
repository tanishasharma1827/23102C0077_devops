package com.sportsbooking.controller;

import com.sportsbooking.model.User;
import com.sportsbooking.service.BookingException;
import com.sportsbooking.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Registration and a simple email sign-in (US1). The signed-in user id is kept in the HTTP session.
 */
@Controller
public class UserController {

    public static final String SESSION_USER = "userId";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user, BindingResult result,
                           HttpSession session, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            User saved = userService.register(user);
            session.setAttribute(SESSION_USER, saved.getId());
            redirect.addFlashAttribute("message", "Registration successful. Welcome, " + saved.getName() + "!");
            return "redirect:/facilities";
        } catch (BookingException e) {
            result.rejectValue("email", "duplicate", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/signin")
    public String signinForm() {
        return "signin";
    }

    @PostMapping("/signin")
    public String signin(@RequestParam String email, HttpSession session, Model model,
                         RedirectAttributes redirect) {
        return userService.findByEmail(email)
                .map(user -> {
                    session.setAttribute(SESSION_USER, user.getId());
                    redirect.addFlashAttribute("message", "Signed in as " + user.getName());
                    return "redirect:/facilities";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "No account found for this email. Please register first.");
                    return "signin";
                });
    }

    @PostMapping("/signout")
    public String signout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
