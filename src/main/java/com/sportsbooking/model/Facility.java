package com.sportsbooking.model;

import jakarta.persistence.*;

@Entity
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /** e.g. BADMINTON, FOOTBALL, TABLE_TENNIS */
    private String type;

    private String location;

    public Facility() {
    }

    public Facility(String name, String type, String location) {
        this.name = name;
        this.type = type;
        this.location = location;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getLocation() { return location; }
}
