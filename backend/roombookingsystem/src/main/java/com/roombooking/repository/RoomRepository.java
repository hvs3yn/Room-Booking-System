package com.roombooking.repository;

import com.roombooking.model.Room;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for rooms. Spring Data JPA builds the queries from the method names. */
public interface RoomRepository extends JpaRepository<Room, Long> {

    /** Finds a room by name, ignoring upper/lower case ("Lab 1" matches "lab 1"). */
    Optional<Room> findByNameIgnoreCase(String name);

    /** Returns true if a room with this name exists, ignoring upper/lower case. */
    boolean existsByNameIgnoreCase(String name);

    /** Returns all rooms sorted by name. */
    List<Room> findAllByOrderByNameAsc();
}
