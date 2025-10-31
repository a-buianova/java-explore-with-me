package ru.practicum.ewm.comments.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing user comments on events.
 * Supports hierarchical replies and moderation workflow (PENDING → PUBLISHED/REJECTED).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 10–2000 chars per DTO; DB column is capped at 2000 to align with validation. */
    @Column(nullable = false, length = 2000)
    private String text;

    /** Author may be null if user was removed (DTO may show "[deleted]"). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    /** Event the comment belongs to; cascade delete on event removal (DB-level). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Event event;

    /** Optional parent comment for replies. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    /** Replies to this comment (no cascade to prevent accidental deletion). */
    @OneToMany(mappedBy = "parentComment")
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();

    /** Moderation state. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CommentState state = CommentState.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime creationDate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updateDate;

    /** Whether comment was edited after creation. */
    @Builder.Default
    private boolean edited = false;

    /** Optimistic lock to avoid concurrent moderation. */
    @Version
    private Long version;
}
