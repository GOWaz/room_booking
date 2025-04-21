package com.example.room_booking.dto;

import com.example.room_booking.model.Booking;
import com.example.room_booking.model.BookingStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Builder
public record BookingResponse(
        Long id,
        String roomNumber,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BookingStatus status,
        BigDecimal totalPrice,
        String customerName

) {
    public static BookingResponse fromEntity(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .roomNumber(booking.getRoom().getRoomNumber())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .totalPrice(calculateTotalPrice(booking))
                .status(booking.getStatus())
                .customerName(booking.getCustomerName())
                .build();
    }

    private static BigDecimal calculateTotalPrice(Booking booking) {
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        return booking.getRoom().getPrice().multiply(BigDecimal.valueOf(nights));
    }
}
