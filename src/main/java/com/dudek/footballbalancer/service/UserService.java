package com.dudek.footballbalancer.service;

import com.dudek.footballbalancer.mapper.UserMapper;
import com.dudek.footballbalancer.model.dto.user.UserSimpleDto;
import com.dudek.footballbalancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(final UserRepository userRepository, final UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserSimpleDto userInfo(final Long userId) {
        return userMapper.userToSimpleDto(userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }
}
