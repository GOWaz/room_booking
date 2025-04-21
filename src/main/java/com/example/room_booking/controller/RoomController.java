package com.example.room_booking.controller;

import com.example.room_booking.model.RoomRequest;
import com.example.room_booking.dto.RoomResponse;
import com.example.room_booking.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("rooms")
@RequiredArgsConstructor
@Tag(name = "Room")
public class RoomController {
    private final RoomService roomService;

    @Operation(
            description = "Get endpoint for everyone",
            summary = "Get all rooms that are available for booking",
            responses = {
                    @ApiResponse(
                            description = "List of All available rooms for booking",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Empty list if no room is available",
                            responseCode = "200",
                            content = @Content()
                    )
            }

    )
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.of(Optional.ofNullable(roomService.getAllAvailable()));
    }

    @Operation(
            description = "Post endpoint for ADMINS only",
            summary = "Add new room",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Room number already exists",
                            responseCode = "400",
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
                            description = "Failed to create room -> for internal server errors",
                            responseCode = "500",
                            content = @Content()
                    ),
            }
    )
    @PostMapping("/add")
    public ResponseEntity<RoomResponse> addRoom(@Valid @RequestBody RoomRequest request) {
        RoomResponse createdRoom = roomService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

}
