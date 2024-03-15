package ru.practicum.events.repository;

import org.springframework.stereotype.Component;
import ru.practicum.events.dto.EventParamsFilt;
import ru.practicum.events.enums.StateEvent;
import ru.practicum.events.model.Event;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class CustomizedEventRepositoryImpl implements CustomizedEventRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Event> publicSearch(EventParamsFilt params) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);

        Predicate criteria = criteriaBuilder.conjunction();

        if (params.getRangeStart() == null) {
            params.setRangeStart(LocalDateTime.now());
        }

        addStateFilter(criteria, criteriaBuilder, eventRoot, List.of(StateEvent.PUBLISHED));
        addTextFilter(criteria, criteriaBuilder, eventRoot, params.getText());
        addCategoryFilter(criteria, criteriaBuilder, eventRoot, params.getCategories());
        addPaidFilter(criteria, criteriaBuilder, eventRoot, params.getPaid());
        addRangeStartFilter(criteria, criteriaBuilder, eventRoot, params.getRangeStart());
        addRangeEndFilter(criteria, criteriaBuilder, eventRoot, params.getRangeEnd());

        criteriaQuery.select(eventRoot).where(criteria);

        return entityManager.createQuery(criteriaQuery)
                .setFirstResult(params.getFrom())
                .setMaxResults(params.getSize())
                .getResultList();
    }


    @Override
    public List<Event> adminSearch(EventParamsFilt params) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);

        Predicate criteria = criteriaBuilder.conjunction();

        addInitiatorsFilter(criteria, criteriaBuilder, eventRoot, params.getIds());
        addStateFilter(criteria, criteriaBuilder, eventRoot, params.getStates());
        addCategoryFilter(criteria, criteriaBuilder, eventRoot, params.getCategories());
        addRangeStartFilter(criteria, criteriaBuilder, eventRoot, params.getRangeStart());
        addRangeEndFilter(criteria, criteriaBuilder, eventRoot, params.getRangeEnd());

        criteriaQuery.select(eventRoot).where(criteria);

        return entityManager.createQuery(criteriaQuery)
                .setFirstResult(params.getFrom())
                .setMaxResults(params.getSize())
                .getResultList();
    }

    private void addTextFilter(Predicate criteria, CriteriaBuilder criteriaBuilder, Root<Event> root, String text) {
        if (Objects.nonNull(text) && !text.isEmpty()) {
            String searchValue = ("%" + text + "%").toLowerCase();
            Predicate annotation = criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), searchValue);
            Predicate description = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchValue);
            criteriaBuilder.and(criteria, criteriaBuilder.or(annotation, description));
        }
    }

    private void addPaidFilter(Predicate criteria, CriteriaBuilder criteriaBuilder, Root<Event> root, Boolean paid) {
        if (Objects.nonNull(paid)) {
            criteriaBuilder.and(criteria, criteriaBuilder.equal(root.get("paid"), paid));
        }
    }

    private void addCategoryFilter(Predicate criteria, CriteriaBuilder criteriaBuilder, Root<Event> root, List<Long> categories) {
        if (!categories.isEmpty()) {
            criteriaBuilder.and(criteria, root.get("category").in(categories));
        }
    }

    private void addInitiatorsFilter(Predicate criteria, CriteriaBuilder criteriaBuilder, Root<Event> root, List<Long> ids) {
        if (!ids.isEmpty()) {
            criteriaBuilder.and(criteria, root.get("initiator").get("id").in(ids));
        }
    }

    private void addStateFilter(Predicate criteria, CriteriaBuilder criteriaBuilder, Root<Event> root, List<StateEvent> states) {
        if (!states.isEmpty()) {
            criteriaBuilder.and(criteria, root.get("state").in(states));
        }
    }

    private void addRangeStartFilter(Predicate criteria, CriteriaBuilder criteriaBuilder, Root<Event> root, LocalDateTime rangeStart) {
        if (rangeStart != null) {
            criteriaBuilder.and(criteria, criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }
    }

    private void addRangeEndFilter(Predicate criteria, CriteriaBuilder criteriaBuilder, Root<Event> root, LocalDateTime rangeEnd) {
        if (rangeEnd != null) {
            criteriaBuilder.and(criteria, criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
    }
}
