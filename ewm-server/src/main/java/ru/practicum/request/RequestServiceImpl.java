package ru.practicum.request;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.StateEvent;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Юзер не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        checkRequestForExists(userId, eventId);
        checkCreatorApproveRequests(userId, event);
        checkStatusOfEvent(event);
        checkParticipantLimit(event);
        boolean isNeedForConfirm = event.getRequestModeration();
        boolean isNeedLimit = event.getParticipantLimit() > 0;
        RequestStatus status;
        if (isNeedForConfirm && isNeedLimit) {
            status = RequestStatus.PENDING;
        } else {
            status = RequestStatus.CONFIRMED;
        }
        Request request = new Request();
        request.setRequesterId(user.getId());
        request.setStatus(status);
        request.setEventId(event.getId());
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getList(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Юзер не найден"));
        List<Request> requestList = requestRepository.findByRequesterId(userId);
        List<ParticipationRequestDto> requestDtoList = RequestMapper.toRequestDtoList(requestList);
        return requestDtoList;
    }

    @Override
    public ParticipationRequestDto delete(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Юзер не найден"));
        Optional<Request> requestOptional = requestRepository.findById(requestId);
        if (requestOptional.isEmpty()) {
            throw new NotFoundException("Запрос участников не найден");
        }
        Request request = requestOptional.get();
        request.setStatus(RequestStatus.CANCELED);
        Request savedRequest = requestRepository.save(request);
        return RequestMapper.toRequestDto(savedRequest);
    }

    private void checkRequestForExists(Long userId, Long eventId) {
        if (requestRepository.findFirstByEventIdAndRequesterId(userId, eventId).isPresent()) {
            throw new ConflictException("Запрос уже существует");
        }
    }

    private void checkCreatorApproveRequests(Long userId, Event event) {
        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException("Создатель события не одобрил делать запросы к событию");
        }
    }

    private void checkStatusOfEvent(Event event) {
        if (!event.getState().equals(StateEvent.PUBLISHED)) {
            throw new ConflictException("Неверный статус события");
        }
    }

    private void checkParticipantLimit(Event event) {
        if (event.getParticipantLimit() > 0) {
            Long participants = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            Integer partLimit = event.getParticipantLimit();
            if (participants >= partLimit) {
                throw new ConflictException("Достигнут лимит участников");
            }
        }
    }
}
