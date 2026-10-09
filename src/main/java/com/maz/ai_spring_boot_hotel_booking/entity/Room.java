package com.maz.ai_spring_boot_hotel_booking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Room entity represents a room in a hotel.
 * Maps to the 'rooms' table in PostgreSQL database.
 * 
 * A Room belongs to one Hotel.
 * A Room can have many Bookings.
 * This is the "many" side of Hotel->Room relationship
 * and the "one" side of Room->Booking relationship.
 */
@Entity
@Table(name = "room")
public class Room {

    /**
     * Primary key: unique identifier for each room.
     * Auto-incremented by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Room number (e.g., 101, 202, 303).
     * @Column(nullable = false, unique = true) means:
     * - required field
     * - each room number must be unique across all hotels (could be refined per hotel)
     */
    @Column(nullable = false, unique = true)
    private String roomNumber;

    /**
     * Nightly price for this room.
     * BigDecimal is used for monetary values to avoid floating-point precision issues.
     * @Column(nullable = false) means price is required.
     */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * Many-to-one relationship: many rooms belong to one hotel.
     * 
     * @ManyToOne tells Hibernate:
     * - This room belongs to exactly one hotel
     * - A foreign key column (hotel_id) will be created in the rooms table
     * 
     * @JoinColumn(name = "hotel_id", nullable = false) specifies:
     * - The foreign key column name in the rooms table is 'hotel_id'
     * - nullable = false means a room must belong to a hotel (required)
     * 
     * fetch = FetchType.EAGER means:
     * - Load the hotel immediately when loading a room
     * - This is common for required many-to-one relationships
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    /**
     * One-to-many relationship: one room can have many bookings.
     * 
     * @OneToMany(mappedBy = "room") tells Hibernate:
     * - This room can have multiple bookings
     * - "room" is the field name in the Booking class that references this room
     * - The Booking class owns the relationship (foreign key is in Booking table)
     * 
     * cascade = CascadeType.ALL means:
     * - If we delete a room, all its bookings are deleted
     * 
     * orphanRemoval = true means:
     * - If we remove a booking from this list, the booking is deleted
     * 
     * fetch = FetchType.LAZY means:
     * - Bookings are only loaded when explicitly accessed (better performance)
     */
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<Booking> bookings;

    /**
     * Constructors
     */
    public Room() {
        // JPA requires a no-arg constructor
    }

    public Room(String roomNumber, BigDecimal price, Hotel hotel) {
        this.roomNumber = roomNumber;
        this.price = price;
        this.hotel = hotel;
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

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}
