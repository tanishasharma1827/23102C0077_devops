package com.sportsbooking.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Slot slot;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.REQUESTED;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Booking() {
    }

    public Booking(User user, Slot slot) {
        this.user = user;
        this.slot = slot;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Slot getSlot() { return slot; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isActive() {
        return status != BookingStatus.CANCELLED;
    }
}
