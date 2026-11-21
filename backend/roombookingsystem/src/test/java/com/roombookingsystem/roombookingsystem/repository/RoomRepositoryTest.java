package com.roombookingsystem.roombookingsystem.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.roombookingsystem.roombookingsystem.model.Room;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

/** Tests RoomRepository against an in-memory H2 database created from schema.sql. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    @DisplayName("save gives the room a database id")
    void save_assignsId() {
        Room saved = roomRepository.save(new Room("Lab 101", 30));

        assertThat(saved.getId()).isNotNull();
        assertThat(roomRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("findByNameIgnoreCase finds a room written with different upper/lower case")
    void findByNameIgnoreCase_findsRoomWithDifferentCase() {
        roomRepository.save(new Room("Lab 101", 30));

        assertThat(roomRepository.findByNameIgnoreCase("lab 101"))
                .isPresent()
                .get()
                .extracting(Room::getName)
                .isEqualTo("Lab 101");
    }

    @Test
    @DisplayName("existsByNameIgnoreCase is true for known names and false for unknown names")
    void existsByNameIgnoreCase_returnsFalseForUnknownName() {
        roomRepository.save(new Room("Lab 101", 30));

        assertThat(roomRepository.existsByNameIgnoreCase("LAB 101")).isTrue();
        assertThat(roomRepository.existsByNameIgnoreCase("Lab 999")).isFalse();
    }

    @Test
    @DisplayName("findAllByOrderByNameAsc returns rooms sorted by name")
    void findAllByOrderByNameAsc_returnsRoomsSortedByName() {
        roomRepository.save(new Room("B-201", 60));
        roomRepository.save(new Room("A-101", 30));
        roomRepository.save(new Room("C-301", 10));

        assertThat(roomRepository.findAllByOrderByNameAsc())
                .extracting(Room::getName)
                .containsExactly("A-101", "B-201", "C-301");
    }

    @Test
    @DisplayName("deleteById removes the room")
    void deleteById_removesRoom() {
        Room saved = roomRepository.save(new Room("Lab 101", 30));

        roomRepository.deleteById(saved.getId());

        assertThat(roomRepository.findById(saved.getId())).isEmpty();
    }
}
