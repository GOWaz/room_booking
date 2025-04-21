package com.example.room_booking.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class BookingRequest {
//    @NotBlank(message = "Customer name is required")
//    @Size(min = 2, max = 100, message = "Name must be between 2-100 characters")
//    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Name can only contain letters, spaces, hyphens, and apostrophes")
//    private String customerName;

    @NotNull(message = "Room ID is required")
    @Positive(message = "Room ID must be a positive number")
    private Long roomId;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;


    @AssertTrue(message = "Check-out date must be after check-in date")
    public boolean isValidDateRange() {
        if (checkInDate == null || checkOutDate == null) {
            return false;
        }
        return checkOutDate.isAfter(checkInDate);
    }


    @AssertTrue(message = "Minimum stay is 1 night")
    public boolean isMinimumStay() {
        if (checkInDate == null || checkOutDate == null) {
            return false;
        }
        return checkOutDate.isAfter(checkInDate.plusDays(1));
    }
}