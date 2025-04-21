package com.example.room_booking.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RoomRequest {
    @Size(min = 1, max = 20, message = "Room number must be between 1 and 20 characters")
    @Pattern(regexp = "^[0-9A-Za-z-]+$", message = "Room number can only contain alphanumeric characters and hyphens")
    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 5, message = "Capacity cannot exceed 100")
    @NotNull(message = "Capacity is required")
    private Integer capacity;

    //    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
//    @Digits(integer = 6, fraction = 2, message = "Price must have up to 6 integer and 2 decimal places")
//    @NotNull(message = "Price is required")
//    private BigDecimal price;
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Digits(integer = 6, fraction = 2, message = "Price must have up to 6 integer and 2 decimal places")
    private BigDecimal price;
}