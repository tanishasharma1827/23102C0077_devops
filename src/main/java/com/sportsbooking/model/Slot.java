package com.sportsbooking.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Facility facility;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public Slot() {
    }

    public Slot(Facility facility, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.facility = facility;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() { return id; }
    public Facility getFacility() { return facility; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }

    public String getLabel() {
        return startTime + " - " + endTime;
    }
}
