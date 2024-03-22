package org.faceit.library.controller;

import lombok.RequiredArgsConstructor;
import org.faceit.library.db.entity.User;
import org.faceit.library.dto.request.UserRequestDTO;
import org.faceit.library.dto.response.UserResponseDTO;
import org.faceit.library.mapper.UserMapper;
import org.faceit.library.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(@PageableDefault Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users.map(userMapper::toResponseDto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserByUserId(@PathVariable Integer userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(userMapper.toResponseDto(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Integer userId, @RequestBody UserRequestDTO userRequestDTO) {
        User user = userMapper.toEntity(userRequestDTO);
        user.setId(userId);
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(userMapper.toResponseDto(savedUser));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserByUserId(@PathVariable Integer userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok().build();
    }
}
