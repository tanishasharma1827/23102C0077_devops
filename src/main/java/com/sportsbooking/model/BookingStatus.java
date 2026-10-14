package com.sportsbooking.model;

/** REQUESTED -> CONFIRMED (by manager); REQUESTED/CONFIRMED -> CANCELLED (by player). */
public enum BookingStatus {
    REQUESTED,
    CONFIRMED,
    CANCELLED
}
