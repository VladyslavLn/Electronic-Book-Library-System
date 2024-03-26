package org.faceit.library.controller;

import lombok.RequiredArgsConstructor;
import org.faceit.library.db.entity.Book;
import org.faceit.library.dto.request.BookRequestDTO;
import org.faceit.library.dto.response.BookResponseDTO;
import org.faceit.library.mapper.BookMapper;
import org.faceit.library.service.BookService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/books")
@RequiredArgsConstructor
public class BooksController {
    private final BookService bookService;
    private final BookMapper bookMapper;

    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@RequestBody BookRequestDTO bookRequestDTO) {
        return new ResponseEntity<>(
                bookMapper.toResponseDTO(
                        bookService.createBook(bookMapper.toEntity(bookRequestDTO))
                ), HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<Page<BookResponseDTO>> getAllBooks(@PageableDefault Pageable pageable) {
        Page<BookResponseDTO> books = bookService.getAllBooks(pageable).map(bookMapper::toResponseDTO);
        return ResponseEntity.ok(books);
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponseDTO> updateBook(@RequestBody BookRequestDTO bookRequestDTO,
                                                      @PathVariable Integer bookId) {
        Book book = bookMapper.toEntity(bookRequestDTO);
        book.setId(bookId);
        return new ResponseEntity<>(
                bookMapper.toResponseDTO(
                        bookService.updateBook(book)), HttpStatus.CREATED
        );
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Integer bookId) {
        Book book = bookService.getBook(bookId);
        return ResponseEntity.ok(bookMapper.toResponseDTO(book));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.ok().build();
    }
}
