package ru.practicum.location;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Location getByLatAndLon(Double lat, Double lon);
}
