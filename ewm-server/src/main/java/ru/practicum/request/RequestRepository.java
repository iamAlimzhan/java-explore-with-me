package ru.practicum.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findFirstByEventIdAndRequesterId(Long eventId, Long userId);

    List<Request> findByEventId(Long eventId);

    List<Request> findAllByIdIn(List<Long> ids);

    List<Request> findByRequesterId(Long userId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus statusRequest);

    @Query("SELECT r.eventId, COUNT(r) FROM Request r WHERE r.eventId IN :eventIds AND r.status = :statusRequest"
            + " GROUP BY r.eventId")
    List<Object[]> countConfirmedRequestsPerEvent(List<Long> eventIds, RequestStatus statusRequest);
}