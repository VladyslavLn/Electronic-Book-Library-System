package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.faceit.library.db.entity.Role;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.RoleRepository;
import org.faceit.library.db.repository.UserRepository;
import org.faceit.library.dto.request.SignInRequest;
import org.faceit.library.dto.request.SignUpRequest;
import org.faceit.library.dto.response.JwtAuthenticationResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        Role userRole = roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("Can't find role with name USER"));
        var user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName())
                .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                .role(Set.of(userRole)).build();
        userRepository.save(user);
        var accessToken = jwtService.generateToken(user);
        return JwtAuthenticationResponse.builder().accessToken(accessToken).build();
    }

    @Override
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        var accessToken = jwtService.generateToken(user);
        return JwtAuthenticationResponse.builder().accessToken(accessToken).build();
    }
}
