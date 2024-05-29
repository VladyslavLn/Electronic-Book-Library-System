package org.faceit.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.faceit.library.dto.request.SignInRequestDTO;
import org.faceit.library.dto.request.SignUpRequestDTO;
import org.faceit.library.dto.response.JwtAuthenticationResponseDTO;
import org.faceit.library.dto.response.SignUpResponseDTO;
import org.faceit.library.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class AuthenticationControllerTest {
    @Value("${api.prefix}")
    private String apiPrefix;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void testRegisterUser() throws Exception {
        SignUpRequestDTO requestDTO = new SignUpRequestDTO();
        requestDTO.setEmail("email@email.com");
        requestDTO.setFirstName("firstName");
        requestDTO.setLastName("lastName");
        requestDTO.setPassword("password");
        SignUpResponseDTO responseDTO = new SignUpResponseDTO();
        responseDTO.setId(1);
        responseDTO.setEmail("email@email.com");
        responseDTO.setFirstName("firstName");
        responseDTO.setLastName("lastName");

        when(authenticationService.register(requestDTO)).thenReturn(responseDTO);

        MvcResult mvcResult = mockMvc.perform(
                        post(apiPrefix + "/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO))
                ).andExpect(status().isOk())
                .andReturn();
        SignUpResponseDTO resultDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), SignUpResponseDTO.class);
        assertEquals(responseDTO.getEmail(), resultDTO.getEmail());
        assertEquals(responseDTO.getFirstName(), resultDTO.getFirstName());
        assertEquals(responseDTO.getLastName(), resultDTO.getLastName());
        assertEquals(responseDTO.getId(), resultDTO.getId());
    }

    @Test
    void testAuthenticate() throws Exception {
        SignInRequestDTO requestDTO = new SignInRequestDTO();
        requestDTO.setEmail("email@example.com");
        requestDTO.setPassword("password");
        JwtAuthenticationResponseDTO responseDTO = JwtAuthenticationResponseDTO.builder().accessToken("token").build();

        when(authenticationService.login(requestDTO)).thenReturn(responseDTO);

        MvcResult mvcResult = mockMvc.perform(
                        post(apiPrefix + "/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO))
                ).andExpect(status().isOk())
                .andReturn();
        JwtAuthenticationResponseDTO resultDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), JwtAuthenticationResponseDTO.class);
        assertEquals(responseDTO.getAccessToken(), resultDTO.getAccessToken());
    }

    @WithMockUser
    @Test
    void testCheckToken() throws Exception {
        mockMvc.perform(get(apiPrefix + "/auth/check-token"))
                .andExpect(status().isOk());
    }

    @Test
    void testCheckToken_notValid() throws Exception {
        mockMvc.perform(get(apiPrefix + "/auth/check-token"))
                .andExpect(status().isForbidden());
    }
}