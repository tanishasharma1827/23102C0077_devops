package com.sportsbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point of the Sports Facility Booking System.
 * Extends SpringBootServletInitializer so the WAR can also be deployed to an external Tomcat.
 */
@SpringBootApplication
public class SportsBookingApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SportsBookingApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(SportsBookingApplication.class);
    }
}
