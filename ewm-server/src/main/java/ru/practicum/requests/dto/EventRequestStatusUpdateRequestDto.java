package ru.practicum.requests.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder(toBuilder = true)
@ToString
public class EventRequestStatusUpdateRequestDto {
    private List<Long> requestIds;
    private String status;
}
