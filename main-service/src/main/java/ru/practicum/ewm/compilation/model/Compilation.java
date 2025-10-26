package ru.practicum.ewm.compilation.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.event.model.Event;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a compilation of events.
 * Compilations can be pinned to the main page.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "compilations")
public class Compilation {

    /** Unique identifier of the compilation. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Title of the compilation. */
    @Column(nullable = false, length = 120)
    private String title;

    /** Whether the compilation is pinned on the main page. */
    @Builder.Default
    @Column(nullable = false)
    private Boolean pinned = false;

    /** Events included in the compilation. */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events = new HashSet<>();
}