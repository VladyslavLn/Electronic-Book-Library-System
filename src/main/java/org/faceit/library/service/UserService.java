package org.faceit.library.service;

import org.faceit.library.db.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {
    UserDetailsService userDetailsService();

    Page<User> getAllUsers(Pageable pageable);

    User getUserById(Integer userId);

    User getUserByEmail(String email);

    void deleteUserById(Integer userId);

    User saveUser(User user);
}
