package ru.practicum.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.Stats;
import ru.practicum.StatsClient;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.StateEvent;
import ru.practicum.event.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ErrorRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.location.Location;
import ru.practicum.location.LocationMapper;
import ru.practicum.location.LocationRepository;
import ru.practicum.request.*;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventPrivateServiceImpl implements EventPrivateService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<EventShortDto> getPrivateList(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Юзер не найден");
        }
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<Event> eventList = eventRepository.findAllByInitiatorId(userId, pageable);
        getStatOfViews(eventList);
        updateConfirmedRequest(eventList);
        return EventMapper.toEventShortDtoForEventsList(eventList);
    }

    @Override
    public EventFullDto getPrivateEventDto(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Юзер не найден");
        }
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие не найдено");
        }

        Event event = eventRepository.getOne(eventId);
        updateConfirmedRequest(List.of(event));

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getPrivateRequestList(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Юзер не найден"));
        List<ParticipationRequestDto> participationRequestDtoList = requestRepository.findByEventId(eventId)
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
        return participationRequestDtoList;
    }

    @Override
    public EventFullDto createPrivateByEventDto(NewEventDto newEventDto, Long userId) {
        Event event = EventMapper.toEvent(newEventDto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Юзер не найден"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        Location location = locationMapper.toLocation(newEventDto.getLocation());
        if (locationRepository.getByLatAndLon(location.getLat(), location.getLon()) == null) {
            locationRepository.save(location);
        }
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setState(StateEvent.PENDING);
        checkDateIsAfter(event.getEventDate(), 2);
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto updatePrivateByEventDto(UpdateEventUserRequest updateRequest, Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Юзер не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (event.getState() == StateEvent.PUBLISHED) {
            throw new ConflictException("Невозможно обновить опубликованное событие");
        }
        updateParamsEvent(event, updateRequest);
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(StateEvent.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(StateEvent.CANCELED);
                    break;
                default:
                    break;
            }
        }
        eventRepository.save(event);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public EventRequestStatusUpdateResult updatePrivateRequestStatus(EventRequestStatusUpdateRequest statusUpdateRequest,
                                                                     Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Юзер не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        List<Long> requestIdList = statusUpdateRequest.getRequestIds();
        EventRequestStatusUpdateResult resultUpdate = new EventRequestStatusUpdateResult();
        boolean isAllowed = event.getRequestModeration() && event.getParticipantLimit() > 0 && !requestIdList.isEmpty();
        if (!isAllowed) {
            return resultUpdate;
        }
        List<Request> requestsToUpdate = requestRepository.findAllByIdIn(requestIdList);
        if (!requestsToUpdate.stream().allMatch(r -> r.getStatus() == RequestStatus.PENDING))
            throw new ConflictException("Невозможно изменить статус запроса");
        RequestStatus requestStatus = RequestStatus.valueOf(String.valueOf(statusUpdateRequest.getStatus()));
        if (requestStatus == RequestStatus.CONFIRMED) {
            updateAndConfirmResult(requestsToUpdate, resultUpdate, event);
        } else if (requestStatus == RequestStatus.REJECTED) {
            requestsToUpdate.forEach(r -> r.setStatus(RequestStatus.REJECTED));
            List<Request> rejectRequests = requestRepository.saveAll(requestsToUpdate);
            resultUpdate.setRejectedRequests(RequestMapper.toRequestDtoList(rejectRequests));
        }
        return resultUpdate;
    }

    private void updateConfirmedRequest(List<Event> eventsList) {
        List<Long> eventIds = new ArrayList<>();
        eventsList.forEach(event -> eventIds.add(event.getId()));
        Map<Long, RequestRepository.ConfirmedRequestProjection> map = requestRepository.countConfirmedRequestsPerEvent(eventIds, RequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(RequestRepository.ConfirmedRequestProjection::getEventId, Function.identity()));
        for (Event event : eventsList) {
            Long eventId = event.getId();
            RequestRepository.ConfirmedRequestProjection projection = map.get(eventId);
            Long confirmedRequestsCount = (projection != null) ? projection.getCount() : 0L;
            event.setConfirmedRequests(confirmedRequestsCount.intValue());
        }
    }

    private void checkDateIsAfter(LocalDateTime date, Integer currentTime) {
        LocalDateTime dateTime = LocalDateTime.now().plusHours(currentTime);
        if (date.isBefore(dateTime)) {
            throw new ErrorRequestException("Неверное время события");
        }
    }

    private void updateParamsEvent(Event event, UpdateEventUserRequest updateEventRequest) {
        if (updateEventRequest.getAnnotation() != null && !updateEventRequest.getAnnotation().isBlank())
            event.setAnnotation(updateEventRequest.getAnnotation());
        if (updateEventRequest.getCategory() != null) {
            Category updatedCategory = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(updatedCategory);
        }
        if (updateEventRequest.getDescription() != null && !updateEventRequest.getDescription().isBlank())
            event.setDescription(updateEventRequest.getDescription());
        if (updateEventRequest.getEventDate() != null) {
            checkDateIsAfter(updateEventRequest.getEventDate(), 1);
            event.setEventDate(updateEventRequest.getEventDate());
        }
        if (updateEventRequest.getLocation() != null) {
            Location updatedLocation = locationMapper.toLocation(updateEventRequest.getLocation());
            if (locationRepository.getByLatAndLon(updatedLocation.getLat(), updatedLocation.getLon()) == null) {
                locationRepository.save(updatedLocation);
            }
            event.setLocation(updatedLocation);
        }
        if (updateEventRequest.getPaid() != null)
            event.setPaid(updateEventRequest.getPaid());
        if (updateEventRequest.getParticipantLimit() != null)
            event.setParticipantLimit(Math.toIntExact(updateEventRequest.getParticipantLimit()));
        if (updateEventRequest.getRequestModeration() != null)
            event.setRequestModeration(updateEventRequest.getRequestModeration());
    }

    private void updateAndConfirmResult(List<Request> requestsToUpdate,
                                        EventRequestStatusUpdateResult result, Event event) {
        long confirmedCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        long limitCount = event.getParticipantLimit();
        for (Request request : requestsToUpdate) {
            if (confirmedCount == limitCount) {
                List<Request> requestsList = requestsToUpdate.subList(requestsToUpdate.indexOf(request), requestsToUpdate.size());
                requestsList.forEach(r -> r.setStatus(RequestStatus.REJECTED));
                List<Request> updatedRequests = requestRepository.saveAll(requestsToUpdate);
                result.setRejectedRequests(RequestMapper.toRequestDtoList(updatedRequests));
                throw new ConflictException("Достигнут лимит участников");
            } else {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedCount++;
            }
        }
        List<Request> updatedRequests = requestRepository.saveAll(requestsToUpdate);
        result.setConfirmedRequests(RequestMapper.toRequestDtoList(updatedRequests));
    }


    public void getStatOfViews(List<Event> events) {
        List<String> uriList = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());
        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        List<Stats> stats = retrieveStats(start, uriList);
        Map<String, Event> uriMap = events.stream()
                .collect(Collectors.toMap(event -> "/events/" + event.getId(), Function.identity()));
        stats.forEach(
                stat -> {
                    String uri = stat.getUri();
                    Event event = uriMap.get(uri);
                    if (event != null) {
                        event.setViews(Math.toIntExact(stat.getHits()));
                    }
                });
    }

    private List<Stats> retrieveStats(LocalDateTime start, List<String> uriList) {
        ResponseEntity<Object> response = statsClient.getStats(start, LocalDateTime.now(), uriList, true);
        if (response.getStatusCode() == HttpStatus.OK) {
            return objectMapper.convertValue(response.getBody(), new TypeReference<>() {
            });
        }
        return Collections.emptyList();
    }
}
