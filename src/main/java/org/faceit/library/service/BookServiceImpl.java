package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.faceit.library.db.entity.Book;
import org.faceit.library.db.entity.BookRating;
import org.faceit.library.db.entity.BookReview;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.BookRepository;
import org.faceit.library.dto.request.BookRatingRequestDTO;
import org.faceit.library.dto.request.BookReviewRequestDTO;
import org.faceit.library.model.BookFileMetadata;
import org.faceit.library.service.exception.BookNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookServiceImpl implements BookService {
    private final S3Service s3Service;
    private final BookRepository bookRepository;
    private final UserService userService;
    private final BookReviewService bookReviewService;
    private final BookRatingService bookRatingService;

    @Override
    public Book updateBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public Book getBook(Integer bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
    }

    @Override
    public void deleteBook(Integer bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
        bookRepository.deleteById(bookId);
        s3Service.deleteObject(book.getFileKey());
    }

    @Override
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Override
    public BookFileMetadata downloadBookFile(Integer bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
        if (StringUtils.isNotBlank(book.getFileKey())) {
            byte[] file = s3Service.getObject(book.getFileKey());
            return new BookFileMetadata(book.getFileKey(), file);
        }
        return null;
    }

    @Override
    public void uploadBookFile(Integer bookId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
        book.setFileKey(originalFilename);
        bookRepository.save(book);
        try {
            s3Service.putObject(originalFilename, file.getBytes());
        } catch (Exception e) {
            log.error("Can't put object to S3 : {}", e.getMessage());
        }
    }

    @Override
    public BookReview addReviewToBook(String username, Integer bookId, BookReviewRequestDTO bookReviewRequestDTO) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
        User user = userService.getUserByEmail(username);
        BookReview bookReview = BookReview.builder()
                .book(book)
                .user(user)
                .reviewContent(bookReviewRequestDTO.getContent())
                .build();
        return bookReviewService.createReview(bookReview);
    }

    @Override
    public BookRating addRatingToBook(String username, Integer bookId, BookRatingRequestDTO bookRatingRequestDTO) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
        User user = userService.getUserByEmail(username);
        BookRating bookRating = BookRating.builder()
                .book(book)
                .user(user)
                .ratingValue(bookRatingRequestDTO.getRatingValue())
                .build();
        return bookRatingService.createBookRating(bookRating);
    }
}
