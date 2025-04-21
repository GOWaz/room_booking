package com.example.room_booking.service;

import com.example.room_booking.model.BookingRequest;
import com.example.room_booking.dto.BookingResponse;
import com.example.room_booking.model.Booking;
import com.example.room_booking.model.BookingStatus;
import com.example.room_booking.repository.BookingRepository;
import com.example.room_booking.model.Room;
import com.example.room_booking.repository.RoomRepository;
import com.example.room_booking.user.User;
import com.example.room_booking.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "bookings", key = "#id")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    if (booking.getRoom() == null) {
                        logger.error("Booking with ID {} has no associated room", id);
                        throw new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Booking data is inconsistent - no room associated"
                        );
                    }
                    return BookingResponse.fromEntity(booking);
                })
                .orElseThrow(() -> {
                    logger.warn("Booking record with id {} not found", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
                });
    }

    @CacheEvict(value = "bookings", allEntries = true)
    @Transactional
    public BookingResponse create(BookingRequest request) {
        try {
            validateBookingRequest(request);

            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> {
                        logger.warn("No room with id {} found", request.getRoomId());
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
                    });

            checkRoomAvailability(room, request.getCheckInDate(), request.getCheckOutDate());

            Booking booking = buildBooking(request, room);
            updateRoomAvailability(room, false);

            Booking savedBooking = bookingRepository.save(booking);
            logBookingConfirmation(savedBooking);

            return BookingResponse.fromEntity(savedBooking);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid booking request: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            logger.warn("Data integrity violation: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room already booked");
        } catch (Exception e) {
            logger.error("Unexpected error creating booking", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create booking"
            );
        }
    }

    @CacheEvict(value = "bookings", allEntries = true)
    @Transactional
    public void cancel(Long id) {
        try {
            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Booking with id {} not found", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
                    });

            if (booking.getStatus() == BookingStatus.CANCELLED) {
                logger.warn("Booking {} is already cancelled", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking already cancelled");
            }

            Room room = booking.getRoom();
            if (room == null) {
                logger.error("Booking {} has no associated room", id);
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Booking data is inconsistent - no room associated"
                );
            }

            updateRoomAvailability(room, true);
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            logger.info("Cancelled booking {}, room {} is now available", id, room.getId());

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to cancel booking {}", id, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to cancel booking"
            );
        }
    }

    private void validateBookingRequest(BookingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Booking request cannot be null");
        }

        validateDates(request.getCheckInDate(), request.getCheckOutDate());
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        LocalDate today = LocalDate.now();

        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Both check-in and check-out dates are required");
        }

        if (checkIn.isBefore(today)) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights < 1) {
            throw new IllegalArgumentException("Minimum stay of 1 night required");
        }
        if (nights > 30) {
            throw new IllegalArgumentException("Maximum stay is 30 nights");
        }
    }

    private void checkRoomAvailability(Room room, LocalDate checkIn, LocalDate checkOut) {
        if (!room.getIsAvailable()) {
            logger.warn("Room {} is not available", room.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room is not available");
        }

        if (bookingRepository.existsConflictingBooking(room.getId(), checkIn, checkOut)) {
            logger.warn("Room {} has booking conflict for dates {} to {}",
                    room.getId(), checkIn, checkOut);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Room is already booked for selected dates"
            );
        }
    }

    private Booking buildBooking(BookingRequest request, Room room) {
        return Booking.builder()
                .customerName(getUserName())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .room(room)
                .status(BookingStatus.CONFIRMED)
                .build();
    }

    private void updateRoomAvailability(Room room, boolean available) {
        room.setIsAvailable(available);
        roomRepository.save(room);
    }

    private void logBookingConfirmation(Booking booking) {
        BigDecimal totalPrice = booking.getRoom().getPrice()
                .multiply(BigDecimal.valueOf(
                        ChronoUnit.DAYS.between(
                                booking.getCheckInDate(),
                                booking.getCheckOutDate()
                        )
                ));

        logger.info("[NOTIFY] Booking created - ID: {}, Name: {}, Room: {}, Dates: {} to {}, Total: ${}, Status: {}",
                booking.getId(),
                booking.getCustomerName(),
                booking.getRoom().getRoomNumber(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                totalPrice,
                booking.getStatus());
    }

    private User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (auth.isAuthenticated() && principal instanceof String) {
            Optional<User> user = userRepository.findByUsername(principal.toString());
            return user.orElse(null);
        } else {
            return (User) principal;
        }
    }

    private String getUserName() {
        String userName = "anonymousUser";
        User user = getUser();
        if (user != null) userName = user.getUsername();
        return userName;
    }
}