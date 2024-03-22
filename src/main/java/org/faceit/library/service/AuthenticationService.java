package org.faceit.library.service;

import org.faceit.library.dto.request.SignInRequest;
import org.faceit.library.dto.request.SignUpRequest;
import org.faceit.library.dto.response.JwtAuthenticationResponse;

public interface AuthenticationService {
    JwtAuthenticationResponse signUp(SignUpRequest request);

    JwtAuthenticationResponse signIn(SignInRequest request);
}
