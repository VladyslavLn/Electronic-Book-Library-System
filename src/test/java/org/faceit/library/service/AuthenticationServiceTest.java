package org.faceit.library.service;

import org.faceit.library.db.entity.Role;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.RoleRepository;
import org.faceit.library.db.repository.UserRepository;
import org.faceit.library.dto.request.SignInRequestDTO;
import org.faceit.library.dto.request.SignUpRequestDTO;
import org.faceit.library.dto.response.JwtAuthenticationResponseDTO;
import org.faceit.library.dto.response.SignUpResponseDTO;
import org.faceit.library.service.exception.UserAlreadyExistException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class AuthenticationServiceTest {
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private RoleRepository roleRepository;
    @Autowired
    private AuthenticationService authenticationService;

    @Test
    void testLogin() {
        String email = "email@email.com";
        String password = "password";
        String token = "token";
        SignInRequestDTO loginRequest = new SignInRequestDTO();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(token);

        JwtAuthenticationResponseDTO login = authenticationService.login(loginRequest);
        assertNotNull(login);
        assertEquals(token, login.getAccessToken());
    }

    @Test
    void testRegister() {
        String firstName = "firstName";
        String lastName = "lastName";
        String email = "email@email.com";
        String password = "password";
        String role = "USER";
        SignUpRequestDTO signUpRequest = new SignUpRequestDTO();
        signUpRequest.setEmail(email);
        signUpRequest.setPassword(password);
        signUpRequest.setFirstName(firstName);
        signUpRequest.setLastName(lastName);
        User user = new User();
        user.setEmail(email);
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(roleRepository.findByName(role)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(password)).thenReturn(password);
        when(userRepository.save(user)).thenReturn(user);

        SignUpResponseDTO responseDTO = authenticationService.register(signUpRequest);
        assertNotNull(responseDTO);
        assertEquals(firstName, responseDTO.getFirstName());
        assertEquals(lastName, responseDTO.getLastName());
        assertEquals(email, responseDTO.getEmail());
    }

    @Test
    void testRegister_shouldThrowExceptionWhenUserWithEmailAlreadyExist() {
        String email = "email@email.com";
        String password = "password";
        SignUpRequestDTO signUpRequest = new SignUpRequestDTO();
        signUpRequest.setEmail(email);
        signUpRequest.setPassword(password);
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistException.class, () -> authenticationService.register(signUpRequest));
    }

    @Test
    void testRegister_shouldThrowExceptionWhenUserRoleIsNotFound() {
        String firstName = "firstName";
        String lastName = "lastName";
        String email = "email@email.com";
        String password = "password";
        String role = "USER";
        SignUpRequestDTO signUpRequest = new SignUpRequestDTO();
        signUpRequest.setEmail(email);
        signUpRequest.setPassword(password);
        signUpRequest.setFirstName(firstName);
        signUpRequest.setLastName(lastName);
        User user = new User();
        user.setEmail(email);
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(roleRepository.findByName(role)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authenticationService.register(signUpRequest));
    }
}