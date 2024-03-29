package org.faceit.library.service;

import org.faceit.library.db.entity.Book;
import org.faceit.library.model.BookFileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {
    Book createBook(Book book);

    Book getBook(Integer bookId);

    Page<Book> getAllBooks(Pageable pageable);

    void deleteBook(Integer bookId);

    Book updateBook(Book entity);

    BookFileMetadata downloadBookFile(Integer bookId);

    void uploadBookFile(Integer bookId, MultipartFile file);
}
