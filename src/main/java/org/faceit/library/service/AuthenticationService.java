package org.faceit.library.service;

import org.faceit.library.dto.request.SignInRequestDTO;
import org.faceit.library.dto.request.SignUpRequestDTO;
import org.faceit.library.dto.response.JwtAuthenticationResponseDTO;
import org.faceit.library.dto.response.SignUpResponseDTO;

public interface AuthenticationService {
    SignUpResponseDTO register(SignUpRequestDTO request);

    JwtAuthenticationResponseDTO login(SignInRequestDTO request);
}
