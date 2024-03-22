package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.faceit.library.db.repository.BookRepository;
import org.faceit.library.service.exception.BookNotFoundException;
import org.faceit.library.service.exception.S3Exception;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final S3Service s3Service;
    private final BookRepository bookRepository;

    @Override
    public List<String> getDownloadedBooksByUserId(Integer userId) {
        return bookRepository.getDownloadedBooksByUserId(userId);
    }

    @Override
    public void uploadBook(Integer userId, MultipartFile file) {
        try {
            s3Service.putObject(file.getOriginalFilename(), file.getBytes());
            bookRepository.saveFilesMetadata(userId, file.getOriginalFilename());
        } catch (Exception e) {
            throw new S3Exception("");
        }
    }

    @Override
    public byte[] getBook(Integer userId, String fileKey) {
        try {
            return s3Service.getObject(fileKey);
        } catch (NoSuchKeyException e) {
            throw new BookNotFoundException(fileKey);
        }
    }

    @Override
    public void deleteBook(Integer userId, String fileKey) {
        s3Service.deleteObject(fileKey);
        bookRepository.deleteFilesMetadata(userId, fileKey);
    }
}
