package org.faceit.library.service.exception;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String fileKey) {
        super("Can't find book with file name: " + fileKey);
    }
}
