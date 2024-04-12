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

import java.util.List;

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
        if (StringUtils.isNotBlank(book.getFileKey())) {
            s3Service.deleteObject(book.getFileKey());
        }
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
        return bookReviewService.saveBookReview(bookReview);
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
        BookRating bookRatingResult = bookRatingService.saveBookRating(bookRating);
        book.getRatings().add(bookRating);
        book.setAvgRating(calculateAverageRating(book.getRatings()));
        bookRepository.save(book);
        return bookRatingResult;
    }

    private double calculateAverageRating(List<BookRating> bookRatings) {
        return bookRatings.stream()
                .mapToInt(BookRating::getRatingValue)
                .average()
                .orElse(0.0);
    }

    @Override
    public BookReview updateBookReview(BookReviewRequestDTO bookReviewRequestDTO, Integer bookReviewId) {
        BookReview bookReview = bookReviewService.getBookReview(bookReviewId);
        bookReview.setReviewContent(bookReviewRequestDTO.getContent());
        return bookReviewService.saveBookReview(bookReview);
    }

    @Override
    public BookRating updateBookRating(BookRatingRequestDTO bookRatingRequestDTO, Integer bookReviewId) {
        BookRating bookRating = bookRatingService.getBookRatingById(bookReviewId);
        bookRating.setRatingValue(bookRatingRequestDTO.getRatingValue());
        bookRatingService.saveBookRating(bookRating);

        Book bookRatingBook = bookRating.getBook();
        List<BookRating> bookRatings = bookRatingBook.getRatings();

        double totalRatingValue = 0;
        for (BookRating rating : bookRatings) {
            totalRatingValue += rating.getRatingValue();
        }
        double averageRating = totalRatingValue / bookRatings.size();

        bookRatingBook.setAvgRating(averageRating);
        bookRepository.save(bookRatingBook);

        return bookRating;
    }

    @Override
    public void deleteBookRating(Integer bookId, Integer bookRatingId) {
        Book book = bookRepository.getReferenceById(bookId);
        bookRatingService.deleteBookRating(bookRatingId);
        book.getRatings().removeIf(rating -> rating.getId().equals(bookRatingId));
        book.setAvgRating(calculateAverageRating(book.getRatings()));
        bookRepository.save(book);
    }

    @Override
    public void deleteBookReview(Integer bookReviewId) {
        bookReviewService.deleteBookReview(bookReviewId);
    }
}
