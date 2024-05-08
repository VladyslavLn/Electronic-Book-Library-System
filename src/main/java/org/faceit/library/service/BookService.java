package org.faceit.library.service;

import org.faceit.library.db.entity.Book;
import org.faceit.library.db.entity.BookRating;
import org.faceit.library.db.entity.BookReview;
import org.faceit.library.dto.request.BookRatingRequestDTO;
import org.faceit.library.dto.request.BookReviewRequestDTO;
import org.faceit.library.model.BookFileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {
    Book createBook(Book book, MultipartFile file);

    Book getBook(Integer bookId);

    Page<Book> getAllBooks(Pageable pageable);

    void deleteBook(Integer bookId);

    Book updateBook(Book entity);

    BookFileMetadata downloadBookFile(Integer bookId);

    void uploadBookFile(Integer bookId, MultipartFile file);

    BookReview addReviewToBook(String username, Integer bookId, BookReviewRequestDTO bookReviewRequestDTO);

    BookRating addRatingToBook(String username, Integer bookId, BookRatingRequestDTO bookRatingRequestDTO);

    BookReview updateBookReview(BookReviewRequestDTO bookReviewRequestDTO, Integer bookReviewId);

    BookRating updateBookRating(BookRatingRequestDTO bookRatingRequestDTO, Integer bookReviewId);

    void deleteBookRating(Integer bookId, Integer bookRatingId);

    void deleteBookReview(Integer bookReviewId);
}
