package ru.practicum.locations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.locations.model.Location;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> getByLatAndLon(Double lat, Double lon);
}