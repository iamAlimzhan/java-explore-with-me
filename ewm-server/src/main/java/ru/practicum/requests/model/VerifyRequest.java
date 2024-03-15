package ru.practicum.requests.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class VerifyRequest {
    private Long count;
    private Long eventId;

    public VerifyRequest(Long count, Long eventId) {
        this.count = count;
        this.eventId = eventId;
    }
}
