package ru.practicum.events.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.events.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, CustomizedEventRepository {
    Optional<Event> findByCategoryId(Long categoryId);

    List<Event> findByInitiatorId(Long initId, Pageable pageable);
}
