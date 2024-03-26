package org.faceit.library.service;

import org.faceit.library.db.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    Book createBook(Book book);

    Book getBook(Integer bookId);

    Page<Book> getAllBooks(Pageable pageable);

    void deleteBook(Integer bookId);

    Book updateBook(Book entity);
}
