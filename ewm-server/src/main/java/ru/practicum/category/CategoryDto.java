package ru.practicum.category;

import lombok.*;
import org.hibernate.validator.constraints.Length;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    @Length(min = 1, max = 50)
    private String name;
}
