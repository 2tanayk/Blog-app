package com.tanay.blogapp.mapper;

import com.tanay.blogapp.dto.AddPostDto;
import com.tanay.blogapp.dto.PostAuthorDto;
import com.tanay.blogapp.dto.PostDto;
import com.tanay.blogapp.entity.Post;
import com.tanay.blogapp.entity.Tag;
import com.tanay.blogapp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(source = "user", target = "author")
    PostDto toDto(Post post);

    PostAuthorDto toAuthorDto(User user);

    /**
     * Maps a Tag entity to its name string.
     * MapStruct auto-discovers this when converting Set<Tag> to List<String>.
     */
    default String map(Tag tag) {
        return tag.getName();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Post toEntity(AddPostDto addPostDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateEntityFromDto(AddPostDto addPostDto, @MappingTarget Post post);
}
