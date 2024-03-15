package ru.practicum.compilations.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
public class NewCompilationDto {
    private Set<Long> events = new HashSet<>();
    private boolean pinned = Boolean.FALSE;
    @NotBlank
    @Length(min = 1, max = 50)
    private String title;
}
