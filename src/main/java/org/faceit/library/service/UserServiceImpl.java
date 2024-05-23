package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.faceit.library.db.entity.Role;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.UserRepository;
import org.faceit.library.dto.request.UserRequestDTO;
import org.faceit.library.service.exception.AccessDeniedException;
import org.faceit.library.service.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public void deleteUserById(Integer userId) {
        boolean isUserExists = userRepository.existsById(userId);
        if (!isUserExists) {
            throw new UserNotFoundException(userId);
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
    public void checkUserAccess(String email, Integer userId) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            boolean isAdmin = user.getRole().stream()
                    .map(Role::getName)
                    .anyMatch(roleName -> roleName.equals("ADMIN"));

            if (!user.getId().equals(userId) && !isAdmin) {
                throw new AccessDeniedException("User does not have access to perform this action");
            }
        }
    }
}
