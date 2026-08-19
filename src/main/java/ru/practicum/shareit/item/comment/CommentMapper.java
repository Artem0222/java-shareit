package ru.practicum.shareit.item.comment;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public Comment toComment(CommentDto dto) {
        if (dto == null) return null;

        Comment comment = new Comment();
        comment.setText(dto.getText());
        return comment;
    }

    public CommentDto toCommentDto(Comment comment) {
        if (comment == null) return null;

        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setCreated(comment.getCreated());

        if (comment.getAuthor() != null) {
            dto.setAuthorName(comment.getAuthor().getName());
        }

        return dto;
    }

    public List<CommentDto> toCommentDtoList(List<Comment> comments) {
        return comments.stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
    }
}