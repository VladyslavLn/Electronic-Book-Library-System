package org.faceit.library.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.faceit.library.db.entity.Role;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.UserRepository;
import org.faceit.library.dto.request.UserRequestDTO;
import org.faceit.library.service.exception.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDetailsService userDetailsService() {
        return userEmail -> userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserById(Integer userId) {
        return userRepository.getReferenceById(userId);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email: " + email + " not found"));
    }

    @Override
    public void deleteUserById(Integer userId) {
        boolean isUserExists = userRepository.existsById(userId);
        if (!isUserExists) {
            throw new EntityNotFoundException("User with id: " + userId + " not found");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public User updateUser(Integer userId, UserRequestDTO userRequestDTO) {
        User userToUpdate = getUserById(userId);
        userToUpdate.setFirstName(userRequestDTO.getFirstName());
        userToUpdate.setLastName(userRequestDTO.getLastName());
        return userRepository.save(userToUpdate);
    }

    @Override
    public void checkUserAccess(String userEmail, Integer userId) {
        User user = getUserByEmail(userEmail);
        boolean isAdmin = user.getRole().stream()
                .map(Role::getName)
                .anyMatch(roleName -> roleName.equals("ADMIN"));

        if (!user.getId().equals(userId) && !isAdmin) {
            throw new AccessDeniedException("User does not have access to perform this action");
        }
    }
}
