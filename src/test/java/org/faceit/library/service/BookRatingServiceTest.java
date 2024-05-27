package org.faceit.library.service;

import org.faceit.library.db.entity.Book;
import org.faceit.library.db.entity.BookRating;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.BookRatingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class BookRatingServiceTest {
    @MockBean
    private BookRatingRepository bookRatingRepository;
    @Autowired
    private BookRatingService bookRatingService;

    @Test
    void testSaveBookRating() {
        BookRating bookRating = createBookRating();

        when(bookRatingRepository.save(bookRating)).thenReturn(bookRating);

        BookRating savedBookRating = bookRatingService.saveBookRating(bookRating);
        assertEquals(bookRating, savedBookRating);
        verify(bookRatingRepository).save(bookRating);
    }

    @Test
    void getBookRatingById() {
        Integer bookRatingId = 1;
        BookRating bookRating = createBookRating();

        when(bookRatingRepository.getReferenceById(bookRatingId)).thenReturn(bookRating);

        BookRating retrievedBookRating = bookRatingService.getBookRatingById(bookRatingId);
        assertEquals(bookRating, retrievedBookRating);
        verify(bookRatingRepository).getReferenceById(bookRatingId);
    }

    @Test
    void deleteBookRatingById() {
        Integer bookRatingId = 1;

        doNothing().when(bookRatingRepository).deleteById(bookRatingId);

        bookRatingService.deleteBookRating(bookRatingId);
        verify(bookRatingRepository).deleteById(bookRatingId);
    }

    private static BookRating createBookRating() {
        BookRating bookRating = new BookRating();
        bookRating.setRatingValue(1);
        Book book = new Book();
        bookRating.setBook(book);
        User user = new User();
        bookRating.setUser(user);
        return bookRating;
    }
}