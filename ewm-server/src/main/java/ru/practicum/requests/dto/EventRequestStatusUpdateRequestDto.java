package ru.practicum.requests.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class EventRequestStatusUpdateRequestDto {
    private List<Long> requestIds;
    private String status;
}
