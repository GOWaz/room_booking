package com.example.room_booking.repository;

import com.example.room_booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE " +
            "b.room.id = :roomId AND " +
            "b.status <> com.example.room_booking.model.BookingStatus.CANCELLED AND " +
            "(:checkIn < b.checkOutDate AND :checkOut > b.checkInDate)")
    boolean existsConflictingBooking(@Param("roomId") Long roomId,
                                     @Param("checkIn") LocalDate checkIn,
                                     @Param("checkOut") LocalDate checkOut);
}
