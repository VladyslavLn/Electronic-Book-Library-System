package org.faceit.library.service;

import jakarta.persistence.EntityNotFoundException;
import org.faceit.library.db.entity.Book;
import org.faceit.library.db.entity.BookRating;
import org.faceit.library.db.entity.BookReview;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.BookRepository;
import org.faceit.library.db.repository.UserRepository;
import org.faceit.library.dto.request.BookRatingRequestDTO;
import org.faceit.library.dto.request.BookRequestDTO;
import org.faceit.library.dto.request.BookReviewRequestDTO;
import org.faceit.library.model.BookFileMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class BookServiceTest {
    @MockBean
    private S3Service s3Service;
    @MockBean
    private BookRepository bookRepository;
    @MockBean
    private BookReviewService bookReviewService;
    @MockBean
    private BookRatingService bookRatingService;
    @MockBean(name = "pdfBookCoverService")
    private BookCoverService pdfBookCoverService;
    @MockBean(name = "epubBookCoverService")
    private BookCoverService epubBookCoverService;
    @MockBean(name = "fb2BookCoverService")
    private BookCoverService fb2BookCoverService;

    @SpyBean
    private UserService userService;
    @Autowired
    private BookService bookService;
    @MockBean
    private UserRepository userRepository;

    @Test
    void testUpdateBook() {
        Integer bookId = 1;
        Book book = createBook();
        String newTitle = "New Title";
        String newAuthor = "New Author";
        String newLanguage = "New Language";
        BookRequestDTO requestDTO = new BookRequestDTO();
        requestDTO.setTitle(newTitle);
        requestDTO.setAuthor(newAuthor);
        requestDTO.setLanguage(newLanguage);
        Book updatedBook = createBook();
        updatedBook.setTitle(newTitle);
        updatedBook.setAuthor(newAuthor);
        updatedBook.setLanguage(newLanguage);

        doNothing().when(userService).checkUserAccess(book.getCreatedBy().getEmail(), book.getCreatedBy().getId());
        when(bookRepository.getReferenceById(bookId)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(updatedBook);

        Book resultBook = bookService.updateBook(bookId, requestDTO, book.getCreatedBy().getEmail());
        assertEquals(newTitle, resultBook.getTitle());
        assertEquals(newAuthor, resultBook.getAuthor());
        assertEquals(newLanguage, resultBook.getLanguage());
    }

    @Test
    void testUpdateBook_shouldThrowExceptionWhenBookNotFound() {
        Integer bookId = 1;
        String newTitle = "New Title";
        String newAuthor = "New Author";
        String newLanguage = "New Language";
        BookRequestDTO requestDTO = new BookRequestDTO();
        requestDTO.setTitle(newTitle);
        requestDTO.setAuthor(newAuthor);
        requestDTO.setLanguage(newLanguage);
        when(bookRepository.getReferenceById(bookId)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> bookService.updateBook(bookId, requestDTO, "user"));
    }

    @Test
    void testGetBook() {
        Book book = createBook();
        Integer bookId = 1;

        when(bookRepository.getReferenceById(bookId)).thenReturn(book);

        Book resultBook = bookService.getBook(bookId);
        assertNotNull(resultBook);
        assertEquals(book, resultBook);
    }

    @Test
    void testDeleteBook() {
        Book book = createBook();
        Integer bookId = 1;

        when(bookRepository.getReferenceById(bookId)).thenReturn(book);
        doNothing().when(bookRepository).deleteById(1);
        doNothing().when(s3Service).deleteObject(book.getFileKey());

        bookService.deleteBook(bookId);
        verify(bookRepository).deleteById(bookId);
        verify(s3Service).deleteObject(book.getFileKey());
    }

    @Test
    void testDeleteBook_ifBookDoesNotHaveFileKey() {
        Book book = createBook();
        book.setFileKey(null);
        Integer bookId = 1;

        when(bookRepository.getReferenceById(bookId)).thenReturn(book);
        doNothing().when(bookRepository).deleteById(1);

        bookService.deleteBook(bookId);
        verify(bookRepository).deleteById(bookId);
        verifyNoInteractions(s3Service);
    }

    @Test
    void testCreateBook_pdf() {
        String newTitle = "New Title";
        String newAuthor = "New Author";
        String language = "Language";
        String userName = "user@email.com";
        Book book = new Book();
        book.setId(1);
        book.setTitle(newTitle);
        book.setAuthor(newAuthor);
        book.setLanguage(language);
        User user = new User();
        user.setEmail(userName);
        MultipartFile file = new MockMultipartFile("file", "file.pdf", "application/pdf", new byte[0]);

        when(userRepository.findByEmail(userName)).thenReturn(Optional.of(user));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);
        doNothing().when(s3Service).putObject(any(), any());
        when(pdfBookCoverService.createBookCover(book)).thenReturn("file.jpg");

        Book createdBook = bookService.createBook(userName, book, file);
        assertNotNull(createdBook);
        assertNotNull(createdBook.getBookCover());
        assertEquals("file.pdf", createdBook.getFileKey());
        assertEquals(newTitle, createdBook.getTitle());
        assertEquals(newAuthor, createdBook.getAuthor());
        assertEquals(language, createdBook.getLanguage());
        assertEquals(userName, createdBook.getCreatedBy().getEmail());
    }

    @Test
    void testCreateBook_epub() {
        String newTitle = "New Title";
        String newAuthor = "New Author";
        String language = "Language";
        String userName = "user@email.com";
        Book book = new Book();
        book.setId(1);
        book.setTitle(newTitle);
        book.setAuthor(newAuthor);
        book.setLanguage(language);
        User user = new User();
        user.setEmail(userName);
        MultipartFile file = new MockMultipartFile("file", "file.epub", "application/epub+zip", new byte[0]);

        when(userRepository.findByEmail(userName)).thenReturn(Optional.of(user));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);
        doNothing().when(s3Service).putObject(any(), any());
        when(epubBookCoverService.createBookCover(book)).thenReturn("file.jpg");

        Book createdBook = bookService.createBook(userName, book, file);
        assertNotNull(createdBook);
        assertNotNull(createdBook.getBookCover());
        assertEquals("file.epub", createdBook.getFileKey());
        assertEquals(newTitle, createdBook.getTitle());
        assertEquals(newAuthor, createdBook.getAuthor());
        assertEquals(language, createdBook.getLanguage());
        assertEquals(userName, createdBook.getCreatedBy().getEmail());
    }

    @Test
    void testCreateBook_fb2() {
        String newTitle = "New Title";
        String newAuthor = "New Author";
        String language = "Language";
        String userName = "user@email.com";
        Book book = new Book();
        book.setId(1);
        book.setTitle(newTitle);
        book.setAuthor(newAuthor);
        book.setLanguage(language);
        User user = new User();
        user.setEmail(userName);
        MultipartFile file = new MockMultipartFile("file", "file.fb2", "application/x-fictionbook+xml", new byte[0]);

        when(userRepository.findByEmail(userName)).thenReturn(Optional.of(user));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);
        doNothing().when(s3Service).putObject(any(), any());
        when(fb2BookCoverService.createBookCover(book)).thenReturn("file.jpg");

        Book createdBook = bookService.createBook(userName, book, file);
        assertNotNull(createdBook);
        assertNotNull(createdBook.getBookCover());
        assertEquals("file.fb2", createdBook.getFileKey());
        assertEquals(newTitle, createdBook.getTitle());
        assertEquals(newAuthor, createdBook.getAuthor());
        assertEquals(language, createdBook.getLanguage());
        assertEquals(userName, createdBook.getCreatedBy().getEmail());
    }

    @Test
    void testCreateBook_throwsExceptionWhenUnsupportedFileFormat() {
        String newTitle = "New Title";
        String newAuthor = "New Author";
        String language = "Language";
        String userName = "user@email.com";
        Book book = new Book();
        book.setId(1);
        book.setTitle(newTitle);
        book.setAuthor(newAuthor);
        book.setLanguage(language);
        User user = new User();
        user.setEmail(userName);
        MultipartFile file = new MockMultipartFile("file", "file.exe", "application/x-fictionbook+xml", new byte[0]);

        when(userRepository.findByEmail(userName)).thenReturn(Optional.of(user));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);

        assertThrows(IllegalArgumentException.class, () -> bookService.createBook(userName, book, file));
    }

    @Test
    void testGetAllBooks() {
        Book book = createBook();
        Page<Book> books = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(any(Pageable.class))).thenReturn(books);

        Page<Book> resultBooks = bookService.getAllBooks(PageRequest.of(0, 10));
        assertEquals(1, resultBooks.getTotalElements());
        assertEquals(1, resultBooks.getContent().size());
        assertEquals(book, resultBooks.getContent().get(0));
    }

    @Test
    void testDownloadBookFile() {
        Book book = createBook();

        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);
        when(s3Service.getObject(book.getFileKey())).thenReturn(new byte[0]);

        BookFileMetadata result = bookService.downloadBookFile(book.getId());
        assertNotNull(result);
        assertEquals(book.getFileKey(), result.getFileName());
        assertArrayEquals(new byte[0], result.getFileData());
    }

    @Test
    void testDownloadBookFile_returnNullWhenBookDoesNotHaveFileKey() {
        Book book = createBook();
        book.setFileKey(null);

        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);

        BookFileMetadata result = bookService.downloadBookFile(book.getId());
        assertNull(result);
    }

    @Test
    void testUploadBookFile() {
        Book book = createBook();
        MultipartFile file = new MockMultipartFile("file", "file.pdf", "application/pdf", new byte[0]);

        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        doNothing().when(s3Service).putObject(any(), any());

        bookService.uploadBookFile(book.getId(), file);

        assertEquals(file.getOriginalFilename(), book.getFileKey());
    }

    @Test
    void testAddReviewToBook() {
        Book book = createBook();
        book.setReviews(new ArrayList<>());
        BookReviewRequestDTO bookReviewRequestDTO = new BookReviewRequestDTO();
        bookReviewRequestDTO.setContent("reviewContent");
        BookReview review = new BookReview();
        review.setBook(book);
        review.setUser(book.getCreatedBy());
        review.setReviewContent("reviewContent");
        review.setId(1);

        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);
        when(userRepository.findByEmail(book.getCreatedBy().getEmail())).thenReturn(Optional.of(book.getCreatedBy()));
        when(bookReviewService.saveBookReview(any())).thenReturn(review);

        BookReview savedReview = bookService.addReviewToBook(book.getCreatedBy().getEmail(), book.getId(), bookReviewRequestDTO);
        assertNotNull(savedReview);
        assertEquals(review, savedReview);
    }

    @Test
    void testAddRatingToBook() {
        Book book = createBook();
        book.setRatings(new ArrayList<>());
        BookRatingRequestDTO bookRatingRequestDTO = new BookRatingRequestDTO();
        bookRatingRequestDTO.setRatingValue(3);
        BookRating rating = new BookRating();
        rating.setRatingValue(3);
        rating.setUser(book.getCreatedBy());
        rating.setBook(book);
        rating.setId(1);

        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);
        when(userRepository.findByEmail(book.getCreatedBy().getEmail())).thenReturn(Optional.of(book.getCreatedBy()));
        when(bookRatingService.saveBookRating(any())).thenReturn(rating);

        BookRating savedRating = bookService.addRatingToBook(book.getCreatedBy().getEmail(), book.getId(), bookRatingRequestDTO);
        assertNotNull(savedRating);
        assertEquals(rating, savedRating);
        assertEquals(3.0, book.getAvgRating());
        assertEquals(1, book.getRatings().size());
    }

    @Test
    void testDeleteBookRating() {
        Book book = createBook();

        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);
        doNothing().when(bookRepository).deleteById(book.getId());
        when(bookRepository.save(book)).thenReturn(book);

        bookService.deleteBookRating(book.getId(), book.getRatings().get(0).getId());

        assertEquals(0.0, book.getAvgRating());
        assertEquals(0, book.getRatings().size());
    }

    @Test
    void testDeleteBookReview() {
        Integer bookReviewId = 1;

        doNothing().when(bookReviewService).deleteBookReview(bookReviewId);

        bookService.deleteBookReview(bookReviewId);
        verify(bookReviewService).deleteBookReview(bookReviewId);
    }

    @Test
    void testGetDownloadedBookByUserName() {
        Book book = createBook();
        User createdBy = book.getCreatedBy();
        String userName = createdBy.getEmail();
        Page<Book> books = new PageImpl<>(List.of(book));

        when(userRepository.findByEmail(userName)).thenReturn(Optional.of(createdBy));
        when(bookRepository.getBooksByCreatedBy(createdBy, PageRequest.of(0, 10))).thenReturn(books);

        Page<Book> resultBooks = bookService.getDownloadedBooksByUserEmail(userName, PageRequest.of(0, 10));
        assertEquals(1, resultBooks.getTotalElements());
        assertEquals(1, resultBooks.getContent().size());
        assertEquals(book, resultBooks.getContent().get(0));
    }

    public static Book createBook() {
        Book book = new Book();
        book.setId(1);
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setLanguage("English");
        book.setFileKey("filekey");
        book.setBookCover("boookCover");
        BookReview bookReview = new BookReview();
        bookReview.setBook(book);
        bookReview.setReviewContent("reviewContent");
        bookReview.setId(1);
        book.setReviews(List.of(bookReview));
        BookRating bookRating = new BookRating();
        bookRating.setBook(book);
        bookRating.setId(1);
        bookRating.setRatingValue(5);
        book.setRatings(List.of(bookRating));
        book.setAvgRating(5.0);
        User user = new User();
        user.setId(1);
        user.setEmail("email@email.com");
        user.setFirstName("FirstName");
        user.setLastName("LastName");
        book.setCreatedBy(user);
        return book;
    }
}