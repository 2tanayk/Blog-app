package com.tanay.blogapp.service;

import com.tanay.blogapp.dto.PostSummaryDto;
import com.tanay.blogapp.dto.UserProfileDto;
import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.exception.ResourceNotFoundException;
import com.tanay.blogapp.mapper.UserMapper;
import com.tanay.blogapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private final UserMapper userMapper;

    public UserProfileDto getUserProfileDetails(Long id) {
        User user = userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User with id " + id + " not found"));

        return userMapper.toDto(user);
    }
}
