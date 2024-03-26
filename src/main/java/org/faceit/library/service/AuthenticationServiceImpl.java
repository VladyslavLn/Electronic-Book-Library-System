package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.faceit.library.db.entity.Role;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.RoleRepository;
import org.faceit.library.db.repository.UserRepository;
import org.faceit.library.dto.request.SignInRequestDTO;
import org.faceit.library.dto.request.SignUpRequestDTO;
import org.faceit.library.dto.response.JwtAuthenticationResponseDTO;
import org.faceit.library.dto.response.SignUpResponseDTO;
import org.faceit.library.service.exception.UserAlreadyExistException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    @Override
    public SignUpResponseDTO register(SignUpRequestDTO request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistException(request.getEmail());
        }
        Role userRole = roleRepository.findByName("USER").orElseThrow(()
                -> new RuntimeException("Can't find role with name USER"));
        var user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName())
                .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                .role(Set.of(userRole)).build();
        userRepository.save(user);
        return new SignUpResponseDTO(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
    }

    @Override
    public JwtAuthenticationResponseDTO login(SignInRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        var accessToken = jwtService.generateToken(user);
        return JwtAuthenticationResponseDTO.builder().accessToken(accessToken).build();
    }
}
