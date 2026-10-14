package com.sportsbooking.controller;

import com.sportsbooking.model.User;
import com.sportsbooking.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Makes the signed-in user available to every page as ${currentUser} (used by the navigation bar). */
@ControllerAdvice
public class CurrentUserAdvice {

    private final UserService userService;

    public CurrentUserAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        Long id = (Long) session.getAttribute(UserController.SESSION_USER);
        return userService.findById(id).orElse(null);
    }
}
