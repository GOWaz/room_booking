package com.example.room_booking.model;

import com.example.room_booking.baseEntity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
//import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "rooms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_rooms_number", columnNames = "room_number")
        })
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@SQLRestriction("is_available = true")
public class Room extends BaseEntity {

    @Column(name = "room_number", nullable = false, length = 40)
    private String roomNumber;

    @Column(nullable = false, length = 4)
    private Integer capacity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @OneToMany(mappedBy = "room")
    @JsonBackReference
    private List<Booking> bookings;
}