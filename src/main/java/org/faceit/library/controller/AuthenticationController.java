package org.faceit.library.controller;

import lombok.RequiredArgsConstructor;
import org.faceit.library.dto.request.SignInRequestDTO;
import org.faceit.library.dto.request.SignUpRequestDTO;
import org.faceit.library.dto.response.JwtAuthenticationResponseDTO;
import org.faceit.library.dto.response.SignUpResponseDTO;
import org.faceit.library.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<SignUpResponseDTO> registerUser(@RequestBody SignUpRequestDTO request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponseDTO> authenticate(@RequestBody SignInRequestDTO request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
}
