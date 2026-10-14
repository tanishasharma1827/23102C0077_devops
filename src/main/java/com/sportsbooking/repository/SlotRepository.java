package com.sportsbooking.repository;

import com.sportsbooking.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByFacilityIdAndDateOrderByStartTime(Long facilityId, LocalDate date);
}
