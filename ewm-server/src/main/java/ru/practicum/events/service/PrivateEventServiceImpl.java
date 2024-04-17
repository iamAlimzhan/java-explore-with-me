package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.events.dto.*;
import ru.practicum.events.enums.StateEvent;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exception.ErrorRequestException;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.locations.dto.LocationDto;
import ru.practicum.locations.mapper.LocationMapper;
import ru.practicum.locations.model.Location;
import ru.practicum.locations.repository.LocationRepository;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.requests.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.enums.StatusRequest;
import ru.practicum.requests.mapper.RequestMapper;
import ru.practicum.requests.model.ConfirmedRequest;
import ru.practicum.requests.model.ParticipationRequest;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.stats_service.StatService;
import ru.practicum.users.enums.StateUser;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateEventServiceImpl implements PrivateService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final RequestMapper requestMapper;
    private final StatService statService;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getByPrivateList(Long userId, Integer from, Integer size) {
        getUserIfExists(userId);
        int page = from / size;
        List<Event> events = eventRepository.findByInitiatorId(userId, PageRequest.of(page, size));
        statService.getViewsList(events);
        getConfirmedRequest(events);
        return new ArrayList<>(eventMapper.toEventShortDtoListForEvents(events));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getByPrivate(Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        getConfirmedRequest(List.of(event));
        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getByPrivateRequests(Long userId, Long eventId) {
        getUserIfExists(userId);
        return requestRepository.findByEventId(eventId)
                .stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createByPrivate(NewEventDto newEventDto, Long userId) {
        Event event = eventMapper.toEvent(newEventDto);
        User user = getUserIfExists(userId);
        Category category = getCategoryIfExists(newEventDto.getCategory());
        Location location = getLocation(newEventDto.getLocation());
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setState(StateEvent.PENDING);
        checkDateTimeIsAfterNowWithGap(event.getEventDate(), 2);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(savedEvent);
    }

    @Override
    @Transactional
    public EventFullDto updateByPrivate(UpdateEventUserRequest updateRequest, Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        if (event.getState() == StateEvent.PUBLISHED) {
            throw new ConflictException("You cannot update a published event");
        }
        updateEventFields(event, updateRequest);
        updateEventStateAction(event, updateRequest.getStateAction());
        eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResultDto updateByPrivateStatus(EventRequestStatusUpdateRequestDto updateRequest,
                                                                   Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        List<Long> requestIds = updateRequest.getRequestIds();
        EventRequestStatusUpdateResultDto result = new EventRequestStatusUpdateResultDto();
        if (!isRequestStatusUpdateAllowed(event, updateRequest)) {
            return result;
        }
        List<ParticipationRequest> requestsToUpdate = requestRepository.findAllByIdIn(requestIds);
        checkAllRequestsPending(requestsToUpdate);
        StatusRequest status = StatusRequest.valueOf(updateRequest.getStatus());
        if (status == StatusRequest.CONFIRMED) {
            confirmAndSetInResult(requestsToUpdate, result, event);
        } else if (status == StatusRequest.REJECTED) {
            rejectAndSetInResult(requestsToUpdate, result);
        }
        return result;
    }

    private boolean isRequestStatusUpdateAllowed(Event event, EventRequestStatusUpdateRequestDto updateRequest) {
        return event.getRequestModeration() &&
                event.getParticipantLimit() > 0 &&
                !updateRequest.getRequestIds().isEmpty();
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void getConfirmedRequest(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        List<Object[]> confirmedRequests = requestRepository.findConfirmedRequest(eventIds);
        Map<Long, Long> confirmedRequestsMap = new HashMap<>();

        for (Object[] result : confirmedRequests) {
            Long eventId = (Long) result[0];
            Long count = (Long) result[1];
            confirmedRequestsMap.put(eventId, count);
        }

        events.forEach(event -> event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L)));
    }


    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found."));
    }

    private Category getCategoryIfExists(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found."));
    }

    private Location getLocation(LocationDto locationDto) {
        Location location = locationMapper.toLocation(locationDto);
        return locationRepository.getByLatAndLon(location.getLat(), location.getLon())
                .orElse(locationRepository.save(location));
    }

    private void checkDateTimeIsAfterNowWithGap(LocalDateTime value, Integer gapFromNowInHours) {
        LocalDateTime minValidDateTime = LocalDateTime.now().plusHours(gapFromNowInHours);
        if (value.isBefore(minValidDateTime)) {
            throw new ErrorRequestException("Invalid event date-time.");
        }
    }

    private void updateEventFields(Event event, UpdateEvent updateEventRequest) {
        updateEventAnnotation(event, updateEventRequest.getAnnotation());
        updateEventCategory(event, updateEventRequest.getCategory());
        updateEventDescription(event, updateEventRequest.getDescription());
        updateEventDate(event, updateEventRequest.getEventDate());
        updateEventLocation(event, updateEventRequest.getLocation());
        updateEventPaidStatus(event, updateEventRequest.getPaid());
        updateEventParticipationLimit(event, updateEventRequest.getParticipantLimit());
        updateEventRequestModeration(event, updateEventRequest.getRequestModeration());
        updateEventTitle(event, updateEventRequest.getTitle());
    }

    private void updateEventTitle(Event event, String title) {
        if (Objects.nonNull(title) && !title.isBlank()) {
            event.setTitle(title);
        }
    }

    private void updateEventStateAction(Event event, StateUser action) {
        if (Objects.nonNull(action)) {
            if (action == StateUser.SEND_TO_REVIEW) {
                event.setState(StateEvent.PENDING);
            } else if (action == StateUser.CANCEL_REVIEW) {
                event.setState(StateEvent.CANCELED);
            }
        }
    }

    private void updateEventRequestModeration(Event event, Boolean requestModeration) {
        if (Objects.nonNull(requestModeration)) {
            event.setRequestModeration(requestModeration);
        }
    }

    private void updateEventParticipationLimit(Event event, Long limit) {
        if (Objects.nonNull(limit)) {
            event.setParticipantLimit(limit);
        }
    }

    private void updateEventPaidStatus(Event event, Boolean paid) {
        if (Objects.nonNull(paid)) {
            event.setPaid(paid);
        }
    }

    private void updateEventLocation(Event event, LocationDto locationDto) {
        if (Objects.nonNull(locationDto)) {
            Location updatedLocation = getLocation(locationDto);
            event.setLocation(updatedLocation);
        }
    }

    private void updateEventDate(Event event, LocalDateTime eventDate) {
        if (Objects.nonNull(eventDate)) {
            checkDateTimeIsAfterNowWithGap(eventDate, 1);
            event.setEventDate(eventDate);
        }
    }

    private void updateEventDescription(Event event, String description) {
        if (Objects.nonNull(description) && !description.isBlank()) {
            event.setDescription(description);
        }
    }

    private void updateEventCategory(Event event, Long catId) {
        if (Objects.nonNull(catId)) {
            Category updated = getCategoryIfExists(catId);
            event.setCategory(updated);
        }
    }

    private void updateEventAnnotation(Event event, String annotation) {
        if (Objects.nonNull(annotation) && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
    }

    private void checkAllRequestsPending(List<ParticipationRequest> requests) {
        boolean allPending = requests.stream()
                .allMatch(r -> r.getStatus() == StatusRequest.PENDING);
        if (!allPending) {
            throw new ConflictException("Impossible to change request status.");
        }
    }

    private void confirmAndSetInResult(List<ParticipationRequest> requestsToUpdate,
                                       EventRequestStatusUpdateResultDto result, Event event) {
        long confirmed = requestRepository.countByEventIdAndStatus(event.getId(), StatusRequest.CONFIRMED);
        long limit = event.getParticipantLimit();

        for (ParticipationRequest request : requestsToUpdate) {
            if (confirmed == limit) {
                int start = requestsToUpdate.indexOf(request);
                int end = requestsToUpdate.size();
                rejectAndSetInResult(requestsToUpdate.subList(start, end), result);
                throw new ConflictException("Participants limit is reached.");
            }
            confirmAndSetInResult(List.of(request), result);
            confirmed++;
        }
    }

    private void confirmAndSetInResult(List<ParticipationRequest> requestsToUpdate,
                                       EventRequestStatusUpdateResultDto result) {
        setStatus(requestsToUpdate, StatusRequest.CONFIRMED);
        List<ParticipationRequest> confirmed = requestRepository.saveAll(requestsToUpdate);
        result.setConfirmedRequests(requestMapper.toRequestDtoList(confirmed));
    }

    private void rejectAndSetInResult(List<ParticipationRequest> requestsToUpdate,
                                      EventRequestStatusUpdateResultDto result) {
        setStatus(requestsToUpdate, StatusRequest.REJECTED);
        List<ParticipationRequest> rejectedRequests = requestRepository.saveAll(requestsToUpdate);
        result.setRejectedRequests(requestMapper.toRequestDtoList(rejectedRequests));
    }

    private void setStatus(List<ParticipationRequest> requestsToUpdate, StatusRequest requestStatus) {
        requestsToUpdate.forEach(r -> r.setStatus(requestStatus));
    }
}
