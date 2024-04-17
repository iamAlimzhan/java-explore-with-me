package ru.practicum.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findFirstByEventIdAndRequesterId(Long eventId, Long userId);

    List<Request> findByEventId(Long eventId);

    List<Request> findAllByIdIn(List<Long> ids);

    List<Request> findByRequesterId(Long userId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus statusRequest);

}