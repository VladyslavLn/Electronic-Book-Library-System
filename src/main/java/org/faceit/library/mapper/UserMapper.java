package org.faceit.library.mapper;

import org.faceit.library.db.entity.User;
import org.faceit.library.dto.response.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {
    @Mapping(target = "roles",
            expression = "java(user.getRole().stream()" +
                    ".map(org.faceit.library.db.entity.Role::getName)" +
                    ".collect(java.util.stream.Collectors.toList()))")
    UserResponseDTO toResponseDTO(User user);
}
