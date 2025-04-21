package com.example.room_booking.service;

import com.example.room_booking.model.Room;
import com.example.room_booking.model.RoomRequest;
import com.example.room_booking.repository.RoomRepository;
import com.example.room_booking.dto.RoomResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
    private final RoomRepository roomRepository;

    @Cacheable("rooms")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<RoomResponse> getAllAvailable() {
        try {
            logger.info("Fetching all available rooms");
            return roomRepository.findAll().stream()
                    .filter(room -> room.getIsAvailable() == true)
                    .map(RoomResponse::fromEntity)
                    .toList();
        } catch (Exception e) {
            logger.error("Failed to fetch rooms", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve rooms"
            );
        }
    }

    @CacheEvict(value = "rooms", allEntries = true)
    @Transactional
    public RoomResponse create(RoomRequest request) {
        try {
            validateRoomRequest(request);

            Room newRoom = Room.builder()
                    .roomNumber(request.getRoomNumber())
                    .capacity(request.getCapacity())
                    .price(request.getPrice())
                    .isAvailable(true)
                    .build();

            Room savedRoom = roomRepository.save(newRoom);
            logger.info("Room created - ID: {}, Number: {}, Price: ${}",
                    savedRoom.getId(),
                    savedRoom.getRoomNumber(),
                    savedRoom.getPrice());

            return RoomResponse.fromEntity(savedRoom);

        } catch (DataIntegrityViolationException e) {
            logger.warn("Duplicate room number: {}", request.getRoomNumber());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Room number already exists"
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid room request: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        } catch (Exception e) {
            logger.error("Error creating room", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create room"
            );
        }
    }

    private void validateRoomRequest(RoomRequest request) {
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        if (request.getRoomNumber() == null || request.getRoomNumber().isBlank()) {
            throw new IllegalArgumentException("Room number is required");
        }

        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new DataIntegrityViolationException("Room number exists");
        }
    }
}