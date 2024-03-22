package org.faceit.library.service;

public interface S3Service {
    void putObject(String key, byte[] file);

    byte[] getObject(String key);

    void deleteObject(String key);
}
