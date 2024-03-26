package org.faceit.library.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtAuthenticationResponseDTO {
    @JsonProperty("access_token")
    private String accessToken;
}
