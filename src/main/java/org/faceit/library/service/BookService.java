package org.faceit.library.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    List<String> getDownloadedBooksByUserId(Integer userId);

    void uploadBook(Integer userId, MultipartFile file);

    byte[] getBook(Integer userId, String fileKey);

    void deleteBook(Integer userId, String fileKey);
}
