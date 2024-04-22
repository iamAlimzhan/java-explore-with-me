package ru.practicum.comments;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentCreateDto {
    @NotBlank
    @Length(min = 10, max = 1000)
    private String text;
}
