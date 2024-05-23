package org.faceit.library.controller;

import lombok.RequiredArgsConstructor;
import org.faceit.library.aop.AuthenticatedUsername;
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
        return ResponseEntity.ok(users.map(userMapper::toResponseDTO));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserByUserId(@AuthenticatedUsername String username,
                                                           @PathVariable("userId") Integer userId) {
        userService.checkUserAccess(username, userId);
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@AuthenticatedUsername String username,
                                                      @PathVariable("userId") Integer userId,
                                                      @RequestBody UserRequestDTO userRequestDTO) {
        userService.checkUserAccess(username, userId);
        User savedUser = userService.updateUser(userId, userRequestDTO);
        return ResponseEntity.ok(userMapper.toResponseDTO(savedUser));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserByUserId(@AuthenticatedUsername String username,
                                                   @PathVariable("userId") Integer userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok().build();
    }
}
