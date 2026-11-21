package com.roombookingsystem.roombookingsystem.config;

import com.roombookingsystem.roombookingsystem.model.Room;
import com.roombookingsystem.roombookingsystem.repository.RoomRepository;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adds a few demo rooms when the application starts, so the app is not empty during the demo.
 * Runs only when {@code app.seed.enabled=true}; rooms that already exist are skipped.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DataSeeder.class);

    private static final Map<String, Integer> DEMO_ROOMS = Map.of(
            "A-101", 30,
            "A-102", 20,
            "B-201", 60,
            "Lab 1", 25,
            "Conference Hall", 150);

    private final RoomRepository roomRepository;

    /** Creates the seeder with the room repository. */
    public DataSeeder(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /** Saves every demo room whose name is not in the database yet. */
    @Override
    public void run(String... args) {
        int added = 0;
        for (Map.Entry<String, Integer> room : DEMO_ROOMS.entrySet()) {
            if (!roomRepository.existsByNameIgnoreCase(room.getKey())) {
                roomRepository.save(new Room(room.getKey(), room.getValue()));
                added++;
            }
        }
        LOG.info("Seeded {} demo rooms.", added);
    }
}
