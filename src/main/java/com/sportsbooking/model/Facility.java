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

    /** Friendly sport name, e.g. TABLE_TENNIS -> Table Tennis. */
    public String getSport() {
        StringBuilder out = new StringBuilder();
        for (String word : type.toLowerCase().split("_")) {
            out.append(out.isEmpty() ? "" : " ").append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    /** Photo shown on the facility card, e.g. /img/table_tennis.jpg */
    public String getImage() {
        return "/img/" + type.toLowerCase() + ".jpg";
    }
}
