package ru.practicum.compilation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    Boolean existsByTitleAndIdNot(String title, Long compilationId);

    @Query("SELECT c FROM Compilation c " +
            "WHERE (:pinned is null OR c.pinned = :pinned)")
    Page<Compilation> findByPinned(@Param("pinned") Boolean pinned, Pageable pageable);
}