package org.faceit.library.service;

import org.faceit.library.db.entity.Role;
import org.faceit.library.db.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class JwtServiceImplTest {
    @MockBean
    private User user;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        when(user.getUsername()).thenReturn("user@mail.com");
        when(user.getId()).thenReturn(1);
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        when(user.getRole()).thenReturn(Collections.singleton(role));
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
    }

    @Test
    void isTokenValid() {
        String token = jwtService.generateToken(user);

        boolean isValid = jwtService.isTokenValid(token, user);

        assertTrue(isValid);
    }
}