package org.faceit.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.faceit.library.JwtUtil;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.UserRepository;
import org.faceit.library.dto.request.UserRequestDTO;
import org.faceit.library.dto.response.UserResponseDTO;
import org.faceit.library.mapper.UserMapper;
import org.faceit.library.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Value("${api.prefix}")
    private String apiPrefix;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MockMvc mockMvc;
    @SpyBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void testGetAllUsers() throws Exception {
        User user = createUser();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(apiPrefix + "/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(page.getTotalElements()))
                .andExpect(jsonPath("$.content[0].firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.content[0].lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.content[0].id").value(user.getId()));
    }

    @Test
    void testGetUserByUserId() throws Exception {
        User user = createUser();
        String token = JwtUtil.createToken(user.getEmail());

        when(userRepository.getReferenceById(1)).thenReturn(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        MvcResult mvcResult = mockMvc.perform(get(apiPrefix + "/users/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        UserResponseDTO userResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserResponseDTO.class);
        assertNotNull(userResponseDTO);
        assertEquals(user.getId(), userResponseDTO.getId());
        assertEquals(user.getFirstName(), userResponseDTO.getFirstName());
        assertEquals(user.getLastName(), userResponseDTO.getLastName());
    }

    @Test
    void testUpdateUser() throws Exception {
        User user = createUser();
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setFirstName("New First Name");
        userRequestDTO.setLastName("New Last Name");
        String token = JwtUtil.createToken(user.getEmail());
        User updatedUser = createUser();
        updatedUser.setFirstName(userRequestDTO.getFirstName());
        updatedUser.setLastName(userRequestDTO.getLastName());

        when(userRepository.getReferenceById(1)).thenReturn(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        MvcResult mvcResult = mockMvc.perform(put(apiPrefix + "/users/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isOk())
                .andReturn();
        UserResponseDTO userResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserResponseDTO.class);
        assertNotNull(userResponseDTO);
        assertEquals(user.getId(), userResponseDTO.getId());
        assertEquals(user.getEmail(), userResponseDTO.getEmail());
        assertEquals(updatedUser.getLastName(), userResponseDTO.getLastName());
        assertEquals(updatedUser.getFirstName(), userResponseDTO.getFirstName());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void testDeleteUserByUserId() throws Exception {
        User user = createUser();
        String token = JwtUtil.createToken(user.getEmail());

        when(userRepository.existsById(1)).thenReturn(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(any(User.class));

        mockMvc.perform(delete(apiPrefix + "/users/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    private User createUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("email@email.com");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setRole(Collections.emptySet());
        return user;
    }
}