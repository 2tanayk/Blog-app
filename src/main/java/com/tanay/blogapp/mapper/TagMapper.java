package com.tanay.blogapp.mapper;

import com.tanay.blogapp.dto.TagsDto;
import com.tanay.blogapp.entity.Tag;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TagMapper {

    // TODO - is there a better way to do this?
    default TagsDto toTagsDto(List<Tag> tags) {
        List<String> tagNames = tags.stream()
                .map(Tag::getName)
                .toList();
        return new TagsDto(tagNames);
    }
}