package org.faceit.library.service;

import org.faceit.library.dto.JwtAuthenticationResponse;
import org.faceit.library.dto.SignInRequest;
import org.faceit.library.dto.SignUpRequest;

public interface AuthenticationService {
    JwtAuthenticationResponse signUp(SignUpRequest request);

    JwtAuthenticationResponse signIn(SignInRequest request);
}