package org.faceit.library.service;

import jakarta.persistence.EntityNotFoundException;
import org.faceit.library.db.entity.Role;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.UserRepository;
import org.faceit.library.dto.request.UserRequestDTO;
import org.faceit.library.service.exception.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Test
    void testUserDetailsService() {
        User user = createUser("USER");
        String userEmail = user.getEmail();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        UserDetailsService userDetails = userService.userDetailsService();
        assertNotNull(userDetails);
    }

    @Test
    void testGetAllUsers() {
        Pageable unpaged = Pageable.unpaged();
        Page<User> users = new PageImpl<>(List.of(createUser("USER")));

        when(userRepository.findAll(unpaged)).thenReturn(users);

        Page<User> result = userService.getAllUsers(unpaged);
        assertNotNull(result);
        assertEquals(result, users);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetUserById() {
        User user = createUser("USER");

        when(userRepository.getReferenceById(user.getId())).thenReturn(user);

        User result = userService.getUserById(user.getId());
        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    void testGetUserByEmail() {
        User user = createUser("USER");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail(user.getEmail());
        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    void testDeleteUserById() {
        when(userRepository.existsById(1)).thenReturn(true);

        userService.deleteUserById(1);

        doNothing().when(userRepository).deleteById(1);
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteUserById_throwExceptionWhenUserNotFound() {
        when(userRepository.existsById(1)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUserById(1));
    }

    @Test
    void testUpdateUser() {
        User user = createUser("USER");
        User updatedUser = createUser("USER");
        updatedUser.setFirstName("updateFirstName");
        updatedUser.setLastName("updateLastName");
        UserRequestDTO updateUserRequestDTO = new UserRequestDTO();
        updateUserRequestDTO.setFirstName("updateFirstName");
        updateUserRequestDTO.setLastName("updateLastName");

        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);

        User result = userService.updateUser(user.getId(), updateUserRequestDTO);
        assertNotNull(result);
        assertNotEquals(user, result);
    }

    @Test
    void testCheckUserAccess() {
        User user = createUser("USER");
        String userEmail = user.getEmail();
        Integer userId = user.getId();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.checkUserAccess(userEmail, userId));
    }

    @Test
    void testCheckUserAccess_userWithAdminRole() {
        User user = createUser("ADMIN");
        String userEmail = user.getEmail();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.checkUserAccess(userEmail, 2));
    }

    @Test
    void testCheckUserAccess_throwExceptionWhenUserDoesNotHaveAccess() {
        User user = createUser("USER");
        String userEmail = user.getEmail();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> userService.checkUserAccess(userEmail, 2));
    }

    private User createUser(String roleName) {
        User user = new User();
        user.setId(1);
        user.setEmail("email@email.com");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setPassword("password");
        user.setCreatedAt(OffsetDateTime.now());
        user.setChangedAt(OffsetDateTime.now());
        Role role = new Role();
        role.setId(1L);
        role.setName(roleName);
        user.setRole(Set.of(role));
        return user;
    }
}