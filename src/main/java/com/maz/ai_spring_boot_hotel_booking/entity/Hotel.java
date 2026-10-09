package com.maz.ai_spring_boot_hotel_booking.entity;

import jakarta.persistence.*;
import java.util.List;

/**
 * Hotel entity represents a hotel in the system.
 * Maps to the 'hotels' table in PostgreSQL database.
 * 
 * A Hotel can have many Rooms.
 * This is the "one" side of a one-to-many relationship.
 */
@Entity
@Table(name = "hotel")
public class Hotel {

    /**
     * Primary key: unique identifier for each hotel.
     * @Id marks this as the primary key.
     * @GeneratedValue(strategy = GenerationType.IDENTITY) tells Hibernate
     * to auto-increment the ID in the database (1, 2, 3, ...).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hotel name.
     * @Column(nullable = false) means this field is required (NOT NULL in database).
     */
    @Column(nullable = false, length=100)
    private String name;

    /**
     * Hotel location (city, address, etc.).
     * @Column(nullable = false) means location is required.
     */
    @Column(nullable = false)
    private String location;

    /**
     * One-to-many relationship: one hotel has many rooms.
     * 
     * @OneToMany(mappedBy = "hotel") tells Hibernate:
     * - This hotel can have multiple rooms
     * - "hotel" is the field name in the Room class that references this hotel
     * - mappedBy means the Room class owns the relationship (foreign key is in Room table). JPA stores the relationship on the owning side.
     * - This side (Hotel) is the inverse side; no database column is created here
     * 
     * cascade = CascadeType.ALL means:
     * - If we delete a hotel, all its rooms are deleted too
     * I removed cascade. Removing cascade restricts cascading from taking a place.
     *
     * orphanRemoval = true means: If we remove a room from this list, the room is deleted from database
     * Use orphanRemoval = true when the child is genuinely owned by the parent and has no meaningful existence independently.
     * orphanRemoval = false (default) → keep orphans, just break relationship. See examples below
     *
     * Without orphanRemoval = true:
     * Hotel hotel = hotelRepository.findById(1L);
     * Room room = hotel.getRooms().get(0);
     *
     * // Remove room from the list
     * hotel.getRooms().remove(room);
     * hotelRepository.save(hotel);  // saves hotel
     *
     * // What happens?
     * // The room is still in the database!
     * // The foreign key (hotel_id) is just set to NULL
     * // You now have an orphaned room (belongs to no hotel)
     *
     * With orphanRemoval = true:
     *
     * Hotel hotel = hotelRepository.findById(1L);
     * Room room = hotel.getRooms().get(0);
     *
     * // Remove room from the list
     * hotel.getRooms().remove(room);
     * hotelRepository.save(hotel);  // saves hotel
     *
     * // What happens?
     * // The room is deleted from the database
     * // No orphaned records left behind
     *
     * fetch = FetchType.LAZY means:
     * - Rooms are only loaded when explicitly accessed (better performance)
     */
    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    private List<Room> rooms;

    /**
     * Constructors
     */
    public Hotel() {
        // JPA requires a no-arg constructor
    }

    public Hotel(String name, String location) {
        this.name = name;
        this.location = location;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
