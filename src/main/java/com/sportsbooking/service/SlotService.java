package com.sportsbooking.service;

import com.sportsbooking.model.Facility;
import com.sportsbooking.model.Slot;
import com.sportsbooking.repository.FacilityRepository;
import com.sportsbooking.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SlotService {

    private final FacilityRepository facilityRepository;
    private final SlotRepository slotRepository;

    public SlotService(FacilityRepository facilityRepository, SlotRepository slotRepository) {
        this.facilityRepository = facilityRepository;
        this.slotRepository = slotRepository;
    }

    public List<Facility> allFacilities() {
        return facilityRepository.findAll();
    }

    public Facility getFacility(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new BookingException("Facility not found"));
    }

    /** Slots of a facility on a date (US2). */
    public List<Slot> availableSlots(Long facilityId, LocalDate date) {
        return slotRepository.findByFacilityIdAndDateOrderByStartTime(facilityId, date);
    }
}
