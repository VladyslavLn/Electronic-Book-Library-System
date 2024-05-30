package org.faceit.library.service;

import org.faceit.library.db.entity.Book;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.faceit.library.service.BookServiceTest.createBook;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class BookCoverServiceTest {
    @Autowired
    private BookCoverService epubBookCoverService;
    @Autowired
    private BookCoverService fb2BookCoverService;
    @Autowired
    private BookCoverService pdfBookCoverService;

    @AfterAll
    static void cleanUp() throws IOException {
        Files.delete(Path.of("src/test/resources/books-cover/epub-book.jpg"));
        Files.delete(Path.of("src/test/resources/books-cover/fb2-book.jpg"));
        Files.delete(Path.of("src/test/resources/books-cover/pdf-book.jpg"));
    }

    @Test
    void testCreateBookCover_epub() {
        Book book = createBook();
        book.setFileKey("epub-book.epub");

        String bookCover = epubBookCoverService.createBookCover(book);

        assertTrue(Files.exists(Path.of("src/test/resources/books-cover/epub-book.jpg")));
        assertNotNull(bookCover);
    }

    @Test
    void testCreateBookCover_pdf() {
        Book book = createBook();
        book.setFileKey("pdf-book.pdf");

        String bookCover = pdfBookCoverService.createBookCover(book);

        assertTrue(Files.exists(Path.of("src/test/resources/books-cover/pdf-book.jpg")));
        assertNotNull(bookCover);
    }

    @Test
    void testCreateBookCover_fb2() {
        Book book = createBook();
        book.setFileKey("fb2-book.fb2");

        String bookCover = fb2BookCoverService.createBookCover(book);

        assertTrue(Files.exists(Path.of("src/test/resources/books-cover/fb2-book.jpg")));
        assertNotNull(bookCover);
    }
}