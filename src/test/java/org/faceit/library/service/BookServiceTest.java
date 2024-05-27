package org.faceit.library.service;

import org.faceit.library.db.entity.Book;
import org.faceit.library.db.entity.BookRating;
import org.faceit.library.db.entity.BookReview;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.BookRepository;
import org.faceit.library.db.repository.UserRepository;
import org.faceit.library.dto.request.BookRequestDTO;
import org.faceit.library.service.exception.BookNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class BookServiceTest {
    @MockBean
    private S3Service s3Service;
    @MockBean
    private BookRepository bookRepository;
    @MockBean
    private BookReviewService bookReviewService;
    @MockBean
    private BookRatingService bookRatingService;
    @MockBean(name = "pdfBookCoverServiceImpl")
    private BookCoverService pdfBookCoverService;
    @MockBean(name = "epubBookCoverServiceImpl")
    private BookCoverService epubBookCoverService;
    @MockBean(name = "fb2BookCoverServiceImpl")
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
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
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
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.updateBook(bookId, requestDTO, "user"));
    }

    @Test
    void testGetBook() {
        Book book = createBook();
        Integer bookId = 1;

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        Book resultBook = bookService.getBook(bookId);
        assertNotNull(resultBook);
        assertEquals(book, resultBook);
    }

    @Test
    void testDeleteBook() {
        Book book = createBook();
        Integer bookId = 1;

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        doNothing().when(bookRepository).deleteById(1);
        doNothing().when(s3Service).deleteObject(book.getFileKey());

        bookService.deleteBook(bookId);
        verify(bookRepository).deleteById(bookId);
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

        Page<Book> resultBooks = bookService.getDownloadedBooksByUsername(userName, PageRequest.of(0, 10));
        assertEquals(1, resultBooks.getTotalElements());
        assertEquals(1, resultBooks.getContent().size());
        assertEquals(book, resultBooks.getContent().get(0));
    }

    private static Book createBook() {
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