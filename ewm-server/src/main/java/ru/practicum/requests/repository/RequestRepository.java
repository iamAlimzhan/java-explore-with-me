package ru.practicum.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.requests.enums.StatusRequest;
import ru.practicum.requests.model.ConfirmedRequest;
import ru.practicum.requests.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findAllByIdIn(List<Long> ids);

    List<ParticipationRequest> findByRequesterId(Long userId);

    @Query("select new ru.practicum.requests.model.ConfirmedRequest(r.event.id,COUNT(distinct r)) " +
            "FROM requests r " +
            "where r.status = 'CONFIRMED' and r.event.id IN :eventsIds group by r.event.id")
    List<ConfirmedRequest> findConfirmedRequest(@Param(value = "eventsIds") List<Long> eventsIds);

    Long countByEventIdAndStatus(Long eventId, StatusRequest statusRequest);

    Optional<ParticipationRequest> findFirstByEventIdAndRequesterId(Long eventId, Long userId);
}
