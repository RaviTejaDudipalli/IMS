package com.ims.service;

import com.ims.dto.response.UserResponse;
import com.ims.entity.User;
import com.ims.exception.ResourceNotFoundException;
import com.ims.mapper.UserMapper;
import com.ims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userMapper.toResponse(requireById(id));
    }

    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled) {
        User user = requireById(id);
        user.setEnabled(enabled);
        log.info("User id={} enabled={}", id, enabled);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = requireById(id);
        userRepository.delete(user);
        log.info("Deleted user id={} username={}", id, user.getUsername());
    }

    private User requireById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
