package ru.practicum.ewm.comments.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.model.Comment;

/**
 * Maps Comment entity to CommentDto.
 * Ignores null nested fields gracefully.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Mapping(target = "author", expression = "java(comment.getAuthor() != null ? comment.getAuthor().getName() : null)")
    @Mapping(target = "eventId", expression = "java(comment.getEvent() != null ? comment.getEvent().getId() : null)")
    @Mapping(target = "parentComment", expression = "java(comment.getParentComment() != null ? comment.getParentComment().getId() : null)")
    CommentDto toDto(Comment comment);
}