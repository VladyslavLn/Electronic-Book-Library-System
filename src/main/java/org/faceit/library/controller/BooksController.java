package org.faceit.library.controller;

import lombok.RequiredArgsConstructor;
import org.faceit.library.aop.AuthenticatedUsername;
import org.faceit.library.db.entity.Book;
import org.faceit.library.db.entity.BookRating;
import org.faceit.library.db.entity.BookReview;
import org.faceit.library.dto.request.BookRatingRequestDTO;
import org.faceit.library.dto.request.BookRequestDTO;
import org.faceit.library.dto.request.BookReviewRequestDTO;
import org.faceit.library.dto.response.BookRatingResponseDTO;
import org.faceit.library.dto.response.BookResponseDTO;
import org.faceit.library.dto.response.BookReviewResponseDTO;
import org.faceit.library.mapper.BookMapper;
import org.faceit.library.mapper.BookRatingMapper;
import org.faceit.library.mapper.BookReviewMapper;
import org.faceit.library.model.BookFileMetadata;
import org.faceit.library.service.BookService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.prefix}/books")
@RequiredArgsConstructor
public class BooksController {
    private final BookService bookService;
    private final BookMapper bookMapper;
    private final BookReviewMapper bookReviewMapper;
    private final BookRatingMapper bookRatingMapper;

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

    @PostMapping("/{bookId}/file/upload")
    public ResponseEntity<String> uploadBook(@PathVariable Integer bookId, @RequestPart MultipartFile file) {
        bookService.uploadBookFile(bookId, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{bookId}/file/download")
    public ResponseEntity<Resource> downloadBook(@PathVariable Integer bookId) {
        BookFileMetadata data = bookService.downloadBookFile(bookId);
        if (null == data) {
            return ResponseEntity.notFound().build();
        }

        String fileName = data.getFileName();
        String contentType;
        if (fileName.endsWith(".pdf")) {
            contentType = "application/pdf";
        } else if (fileName.endsWith(".epub")) {
            contentType = "application/epub+zip";
        } else if (fileName.endsWith(".fb2")) {
            contentType = "application/x-fictionbook+xml";
        } else {
            throw new IllegalStateException("Unsupported file type.");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(new ByteArrayResource(data.getFileData()));
    }

    @PostMapping("/{bookId}/review")
    public ResponseEntity<BookReviewResponseDTO> addReviewToBook(@AuthenticatedUsername String username,
                                                                 @PathVariable Integer bookId,
                                                                 @RequestBody BookReviewRequestDTO bookReviewRequestDTO) {
        BookReview bookReview = bookService.addReviewToBook(username, bookId, bookReviewRequestDTO);
        return ResponseEntity.ok(bookReviewMapper.toDto(bookReview));
    }

    @PostMapping("/{bookId}/rating")
    public ResponseEntity<BookRatingResponseDTO> addRatingToBook(@AuthenticatedUsername String username, @PathVariable Integer bookId,
                                                                 @RequestBody BookRatingRequestDTO bookRatingRequestDTO) {
        BookRating bookRating = bookService.addRatingToBook(username, bookId, bookRatingRequestDTO);
        return ResponseEntity.ok(bookRatingMapper.toDto(bookRating));
    }
}
