package ru.practicum.requests.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmedRequest {
    private Long count;
    private Long eventId;

    public ConfirmedRequest(Long eventId, Long count) {
        this.eventId = eventId;
        this.count = count;
    }
}