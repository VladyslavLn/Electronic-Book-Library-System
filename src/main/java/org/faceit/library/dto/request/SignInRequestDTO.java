package org.faceit.library.dto.request;

import lombok.Data;

@Data
public class SignInRequestDTO {
    private String email;
    private String password;
}
