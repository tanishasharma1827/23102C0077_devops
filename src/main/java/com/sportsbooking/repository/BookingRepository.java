package com.sportsbooking.repository;

import com.sportsbooking.model.Booking;
import com.sportsbooking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsBySlotIdAndStatusIn(Long slotId, Collection<BookingStatus> statuses);

    List<Booking> findBySlotIdInAndStatusIn(Collection<Long> slotIds, Collection<BookingStatus> statuses);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByStatusOrderByCreatedAt(BookingStatus status);
}
