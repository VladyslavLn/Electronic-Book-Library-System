package org.faceit.library.service.exception;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String fileKey) {
        super("Can't find book with file name: " + fileKey);
    }
    public BookNotFoundException(Integer bookId) {
        super("Can't find book by id: " + bookId);
    }
}
