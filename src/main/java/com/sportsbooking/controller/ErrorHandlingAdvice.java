package com.sportsbooking.controller;

import com.sportsbooking.service.BookingException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** M14: show a friendly error page instead of a stack trace for bad input. */
@ControllerAdvice
public class ErrorHandlingAdvice {

    @ExceptionHandler(BookingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String businessError(BookingException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error-page";
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String badRequest(Model model) {
        model.addAttribute("error", "The request was not valid. Please go back and try again.");
        return "error-page";
    }
}
