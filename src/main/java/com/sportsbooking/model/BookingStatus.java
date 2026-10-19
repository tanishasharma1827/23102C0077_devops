package com.sportsbooking.model;

/**
 * REQUESTED -> CONFIRMED or REJECTED (by manager);
 * REQUESTED/CONFIRMED -> CANCELLED (by the player, or by the manager).
 */
public enum BookingStatus {
    REQUESTED,
    CONFIRMED,
    REJECTED,
    CANCELLED
}
