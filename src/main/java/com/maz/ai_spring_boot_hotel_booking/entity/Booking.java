package com.maz.ai_spring_boot_hotel_booking.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Booking entity represents a hotel room booking.
 * Maps to the 'bookings' table in PostgreSQL database.
 * 
 * A Booking belongs to one Room.
 * This is the "many" side of a one-to-many relationship from Room.
 *
 *
 *  When does validation actually run? Only when the controller parameter has @Valid
 *
 */
@Entity
@Table(name = "booking")
public class Booking {

    /**
     * Primary key: unique identifier for each booking.
     * Auto-incremented by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Guest name: name of the person making the booking.
     * @Column(nullable = false) means guest name is required.
     */
    @Column(nullable = false)
    private String guestName;

    /**
     * Check-in date: the date the guest arrives.
     * LocalDate is used for date-only values (no time component).
     * @Column(nullable = false) means check-in date is required.
     */
    @Column(nullable = false)
    private LocalDate checkInDate;

    /**
     * Check-out date: the date the guest leaves.
     * @Column(nullable = false) means check-out date is required.
     */
    @Column(nullable = false)
    private LocalDate checkOutDate;

    /**
     * Many-to-one relationship: many bookings belong to one room.
     * 
     * @ManyToOne tells Hibernate:
     * - This booking is associated with exactly one room
     * - A foreign key column (room_id) will be created in the bookings table
     * 
     * @JoinColumn(name = "room_id", nullable = false) specifies:
     * - The foreign key column name in the bookings table is 'room_id'
     * - nullable = false means every booking must have a room (required)
     * 
     * fetch = FetchType.EAGER means:
     * - Load the room immediately when loading a booking
     * - This makes sense because you almost always want to know which room is booked
     * - For @ManyToOne, EAGER is the default anyway. It makes sense here because a booking always needs its room info, so loading both together is efficient.
     * - When you load a Booking, Hibernate will immediately load the associated Room as well.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /**
     * Constructors
     */
    public Booking() {
        // JPA requires a no-arg constructor
    }

    public Booking(String guestName, LocalDate checkInDate, LocalDate checkOutDate, Room room) {
        this.guestName = guestName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.room = room;
    }

    /**
     * Getters and Setters
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
