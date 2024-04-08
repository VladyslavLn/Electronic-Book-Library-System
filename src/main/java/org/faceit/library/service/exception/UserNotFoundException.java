package org.faceit.library.service.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Integer userId) {
        super("User with id: " + userId + " does not exist");
    }

    public UserNotFoundException(String email) {
        super("User with email: " + email + " does not exist");
    }
}
