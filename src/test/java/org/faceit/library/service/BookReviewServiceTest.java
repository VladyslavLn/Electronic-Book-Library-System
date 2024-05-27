package org.faceit.library.service;

import org.faceit.library.db.entity.Book;
import org.faceit.library.db.entity.BookReview;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.BookReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class BookReviewServiceTest {
    @MockBean
    private BookReviewRepository bookReviewRepository;
    @Autowired
    private BookReviewService bookReviewService;

    @Test
    void testSaveBookReview() {
        BookReview bookReview = createBookReview();

        when(bookReviewRepository.save(bookReview)).thenReturn(bookReview);

        BookReview saveBookReview = bookReviewService.saveBookReview(bookReview);
        assertEquals(bookReview, saveBookReview);
        verify(bookReviewRepository).save(bookReview);
    }

    @Test
    void testGetBookReviewById() {
        Integer bookReviewId = 1;
        BookReview bookReview = createBookReview();

        when(bookReviewRepository.getReferenceById(bookReviewId)).thenReturn(bookReview);

        BookReview retrievedBookReview = bookReviewService.getBookReview(bookReviewId);
        assertEquals(bookReview, retrievedBookReview);
        verify(bookReviewRepository).getReferenceById(bookReviewId);
    }

    @Test
    void testDeleteBookReviewById() {
        Integer bookReviewId = 1;

        doNothing().when(bookReviewRepository).deleteById(bookReviewId);

        bookReviewService.deleteBookReview(bookReviewId);
        verify(bookReviewRepository).deleteById(bookReviewId);
    }

    private static BookReview createBookReview() {
        BookReview bookReview = new BookReview();
        bookReview.setReviewContent("Review content");
        Book book = new Book();
        bookReview.setBook(book);
        User user = new User();
        bookReview.setUser(user);
        return bookReview;
    }
}