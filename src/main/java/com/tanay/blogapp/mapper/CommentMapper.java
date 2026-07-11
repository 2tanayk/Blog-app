package com.tanay.blogapp.mapper;

import com.tanay.blogapp.dto.AddCommentDto;
import com.tanay.blogapp.dto.CommentDto;
import com.tanay.blogapp.dto.PostAuthorDto;
import com.tanay.blogapp.entity.Comment;
import com.tanay.blogapp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "user", target = "author")
    CommentDto toDto(Comment comment);

    PostAuthorDto toAuthorDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Comment toEntity(AddCommentDto addCommentDto);
}