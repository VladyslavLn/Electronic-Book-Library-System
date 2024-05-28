package org.faceit.library.service;

import org.faceit.library.db.entity.Book;
import org.faceit.library.db.entity.BookRating;
import org.faceit.library.db.entity.BookReview;
import org.faceit.library.dto.request.BookRatingRequestDTO;
import org.faceit.library.dto.request.BookRequestDTO;
import org.faceit.library.dto.request.BookReviewRequestDTO;
import org.faceit.library.model.BookFileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {
    Book createBook(String userEmail, Book book, MultipartFile file);

    Book getBook(Integer bookId);

    Page<Book> getAllBooks(Pageable pageable);

    void deleteBook(Integer bookId);

    Book updateBook(Integer bookId, BookRequestDTO requestDTO, String userEmail);


    BookFileMetadata downloadBookFile(Integer bookId);

    void uploadBookFile(Integer bookId, MultipartFile file);

    BookReview addReviewToBook(String userEmail, Integer bookId, BookReviewRequestDTO bookReviewRequestDTO);

    BookRating addRatingToBook(String userEmail, Integer bookId, BookRatingRequestDTO bookRatingRequestDTO);

    void deleteBookRating(Integer bookId, Integer bookRatingId);

    void deleteBookReview(Integer bookReviewId);

    Page<Book> getDownloadedBooksByUserEmail(String userEmail, Pageable pageable);
}
