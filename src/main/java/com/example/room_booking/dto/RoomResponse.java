package com.example.room_booking.dto;

import com.example.room_booking.model.Room;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RoomResponse(
        Long id,
        String roomNumber,
        Integer capacity,
        BigDecimal price,
        Boolean isAvailable
) {
    public static RoomResponse fromEntity(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .capacity(room.getCapacity())
                .price(room.getPrice())
                .isAvailable(room.getIsAvailable())
                .build();
    }
}
