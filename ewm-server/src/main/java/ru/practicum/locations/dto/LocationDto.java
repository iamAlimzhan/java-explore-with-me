package ru.practicum.locations.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@RequiredArgsConstructor
public class LocationDto {
    @NotNull
    private final Double lat;

    @NotNull
    private final Double lon;
}
