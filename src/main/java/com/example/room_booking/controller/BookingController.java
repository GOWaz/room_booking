package com.example.room_booking.controller;

import com.example.room_booking.model.BookingRequest;
import com.example.room_booking.dto.BookingResponse;
import com.example.room_booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("bookings")
@RequiredArgsConstructor
@Tag(name = "Booking")
public class BookingController {
    private final BookingService bookingService;

    @Operation(
            description = "Get endpoint for ADMINS or USERS",
            summary = "Get Booking record by id",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Booking not found",
                            responseCode = "404",
                            content = @Content()
                    ),
                    @ApiResponse(
                            description = "Forbidden -> Not authenticated",
                            responseCode = "403",
                            content = @Content()
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @Operation(
            description = "Post endpoint for ADMINS Or USER",
            summary = "Book a room",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "201"

                    ),
                    @ApiResponse(
                            description = "Room not found",
                            responseCode = "404",
                            content = @Content()
                    ),
                    @ApiResponse(
                            description = "Failed -> wrong argument",
                            responseCode = "400",
                            content = @Content()
                    ),
                    @ApiResponse(
                            description = "Forbidden -> Not authenticated",
                            responseCode = "403",
                            content = @Content()
                    ),
                    @ApiResponse(
                            description = "Failed to create booking -> for internal server errors",
                            responseCode = "500",
                            content = @Content()
                    ),
            }
    )
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        BookingResponse response = bookingService.create(bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            description = "Put endpoint for ADMINS or USERS",
            summary = "Cancel booking",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "202",
                            content = @Content()
                    ),
                    @ApiResponse(
                            description = "Booking not found",
                            responseCode = "404",
                            content = @Content()
                    ),
                    @ApiResponse(
                            description = "Booking already cancelled",
                            responseCode = "400",
                            content = @Content()
                    ),
                    @ApiResponse(
                            description = "Forbidden -> Not authenticated",
                            responseCode = "403",
                            content = @Content()
                    ),
                    @ApiResponse(
                            description = "Booking data is inconsistent - no room associated",
                            responseCode = "403",
                            content = @Content()
                    ),
                    @ApiResponse(
                            description = "Failed to cancel booking -> for internal server errors",
                            responseCode = "500",
                            content = @Content()
                    ),
            }
    )
    @PutMapping("/cancel/{id}")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        bookingService.cancel(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
