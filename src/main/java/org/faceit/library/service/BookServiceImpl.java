package org.faceit.library.service;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
@Transactional
public class BookServiceImpl implements BookService {
    private final S3Service s3Service;
    private final BookRepository bookRepository;
    private final UserService userService;
    private final BookReviewService bookReviewService;
    private final BookRatingService bookRatingService;
    private final BookCoverService pdfBookCoverServiceImpl;
    private final BookCoverService epubBookCoverServiceImpl;
    private final BookCoverService fb2BookCoverServiceImpl;

    public BookServiceImpl(S3Service s3Service, BookRepository bookRepository,
                           UserService userService, BookReviewService bookReviewService,
                           BookRatingService bookRatingService,
                           @Qualifier("pdfBookCoverServiceImpl")
                           BookCoverService pdfBookCoverService,
                           @Qualifier("epubBookCoverServiceImpl")
                           BookCoverService epubBookCoverService,
                           @Qualifier("fb2BookCoverServiceImpl")
                           BookCoverService fb2BookCoverService) {
        this.s3Service = s3Service;
        this.bookRepository = bookRepository;
        this.userService = userService;
        this.bookReviewService = bookReviewService;
        this.bookRatingService = bookRatingService;
        this.pdfBookCoverServiceImpl = pdfBookCoverService;
        this.epubBookCoverServiceImpl = epubBookCoverService;
        this.fb2BookCoverServiceImpl = fb2BookCoverService;
    }

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
    public Book createBook(String username, Book book, MultipartFile file) {
        User user = userService.getUserByEmail(username);
        book.setCreatedBy(user);
        Book savedBook = bookRepository.save(book);
        uploadBookFile(savedBook.getId(), file);
        String bookCoverFileKey = null;
        String fileExtension = getFileExtension(file);
        if (StringUtils.isNotBlank(fileExtension)) {
            bookCoverFileKey = switch (fileExtension) {
                case "pdf" -> pdfBookCoverServiceImpl.createBookCover(book);
                case "epub" -> epubBookCoverServiceImpl.createBookCover(book);
                case "fb2" -> fb2BookCoverServiceImpl.createBookCover(book);
                default -> throw new IllegalArgumentException("Unsupported file extension: " + fileExtension);
            };
        }
        savedBook.setBookCover(bookCoverFileKey);
        return bookRepository.save(savedBook);
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

    @Override
    public Page<Book> getDownloadedBooksByUsername(String username, Pageable pageable) {
        User user = userService.getUserByEmail(username);
        return bookRepository.getBooksByCreatedBy(user, pageable);
    }

    private String getFileExtension(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName != null) {
            int lastDot = originalFileName.lastIndexOf('.');
            if (lastDot >= 0) {
                return originalFileName.substring(lastDot + 1);
            }
        }
        return null;
    }
}
