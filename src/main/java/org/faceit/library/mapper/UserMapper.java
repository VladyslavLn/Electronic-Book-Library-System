package org.faceit.library.mapper;

import org.faceit.library.db.entity.User;
import org.faceit.library.dto.request.UserRequestDTO;
import org.faceit.library.dto.response.UserResponseDTO;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserResponseDTO toResponseDTO(User user);

    User toEntity(UserRequestDTO userRequestDTO);
}
