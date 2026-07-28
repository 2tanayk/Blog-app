package com.tanay.blogapp.mapper;

import com.tanay.blogapp.dto.UserDetailsDto;
import com.tanay.blogapp.dto.UserProfileDto;
import com.tanay.blogapp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "createdAt", target = "joinedDate")
    UserProfileDto toUserProfileDto(User user);

    @Mapping(source = "createdAt", target = "joinedDate")
    UserDetailsDto toUserDetailsDto(User user);
}
