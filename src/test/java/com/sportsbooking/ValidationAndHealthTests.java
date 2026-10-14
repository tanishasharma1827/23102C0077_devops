package com.sportsbooking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationAndHealthTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unknownFacilityShowsFriendlyError() throws Exception {
        mockMvc.perform(get("/slots").param("facilityId", "9999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Facility not found")));
    }

    @Test
    void invalidFacilityIdShowsFriendlyError() throws Exception {
        mockMvc.perform(get("/slots").param("facilityId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("not valid")));
    }

    @Test
    void healthEndpointIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void infoEndpointShowsVersion() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("Sports Facility Booking System"));
    }
}
