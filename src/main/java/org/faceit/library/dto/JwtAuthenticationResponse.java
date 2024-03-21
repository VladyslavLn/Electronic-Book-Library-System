package org.faceit.library.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtAuthenticationResponse {
    @JsonProperty("access_token")
    private String accessToken;
}
