package ru.practicum.ewm.category.model;

import jakarta.persistence.*;
import lombok.*;

/** Entity representing an event category. */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Category name must be unique and non-null (max 50 chars). */
    @Column(nullable = false, length = 50, unique = true)
    private String name;
}