package ru.practicum.compilation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    Boolean existsByTitleAndIdNot(String title, Long compilationId);
}