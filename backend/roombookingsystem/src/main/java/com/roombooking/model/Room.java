package com.roombooking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A university room that can be booked. Mapped to the {@code rooms} table. */
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false)
    private int capacity;

    /** Required by JPA; not for application code. */
    protected Room() {
    }

    /** Creates a new, not yet saved room (the database assigns the id). */
    public Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    /** Returns the database id, or {@code null} before the room is saved. */
    public Long getId() {
        return id;
    }

    /** Returns the room name, for example "Lab 101". */
    public String getName() {
        return name;
    }

    /** Returns the maximum number of people the room holds. */
    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return "Room{id=" + id + ", name='" + name + "', capacity=" + capacity + "}";
    }
}
