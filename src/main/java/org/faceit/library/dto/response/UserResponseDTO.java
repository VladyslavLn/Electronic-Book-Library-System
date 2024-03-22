package org.faceit.library.dto.response;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
}
