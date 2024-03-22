package org.faceit.library.service.exception;

public class S3Exception extends RuntimeException {
    public S3Exception(String message) {
        super(message);
    }
}
