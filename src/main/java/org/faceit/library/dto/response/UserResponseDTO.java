package org.faceit.library.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class UserResponseDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
}
