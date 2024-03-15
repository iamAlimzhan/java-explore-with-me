package ru.practicum.compilations.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
public class UpdateCompilationRequest {
    private Set<Long> events = new HashSet<>();
    private Boolean pinned;
    @Length(min = 1, max = 50)
    private String title;
}
