package com.sportsbooking.service;

/** A business rule was broken (duplicate email, slot already booked, ...). The message is shown to the user. */
public class BookingException extends RuntimeException {

    public BookingException(String message) {
        super(message);
    }
}
