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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponseDTO> createBook(@AuthenticatedUsername String username, @RequestPart("book") BookRequestDTO bookRequestDTO,
                                                      @RequestPart("file") MultipartFile file) {
        return new ResponseEntity<>(
                bookMapper.toResponseDTO(
                        bookService.createBook(username, bookMapper.toEntity(bookRequestDTO), file)
                ), HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<Page<BookResponseDTO>> getAllBooks(@PageableDefault(sort = {"id"}) Pageable pageable) {
        Page<BookResponseDTO> books = bookService.getAllBooks(pageable).map(bookMapper::toResponseDTO);
        return ResponseEntity.ok(books);
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponseDTO> updateBook(@RequestBody BookRequestDTO bookRequestDTO,
                                                      @PathVariable("bookId") Integer bookId) {
        Book book = bookMapper.toEntity(bookRequestDTO);
        book.setId(bookId);
        return new ResponseEntity<>(
                bookMapper.toResponseDTO(
                        bookService.updateBook(book)), HttpStatus.CREATED
        );
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable("bookId") Integer bookId) {
        Book book = bookService.getBook(bookId);
        return ResponseEntity.ok(bookMapper.toResponseDTO(book));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable("bookId") Integer bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{bookId}/file/upload")
    public ResponseEntity<String> uploadBook(@PathVariable("bookId") Integer bookId, @RequestPart MultipartFile file) {
        bookService.uploadBookFile(bookId, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{bookId}/file/download")
    public ResponseEntity<Resource> downloadBook(@PathVariable("bookId") Integer bookId) {
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
                                                                 @PathVariable("bookId") Integer bookId,
                                                                 @RequestBody BookReviewRequestDTO bookReviewRequestDTO) {
        BookReview bookReview = bookService.addReviewToBook(username, bookId, bookReviewRequestDTO);
        return ResponseEntity.ok(bookReviewMapper.toDto(bookReview));
    }

    @PostMapping("/{bookId}/rating")
    public ResponseEntity<BookRatingResponseDTO> addRatingToBook(@AuthenticatedUsername String username, @PathVariable("bookId") Integer bookId,
                                                                 @RequestBody BookRatingRequestDTO bookRatingRequestDTO) {
        BookRating bookRating = bookService.addRatingToBook(username, bookId, bookRatingRequestDTO);
        return ResponseEntity.ok(bookRatingMapper.toDto(bookRating));
    }

    @DeleteMapping("/{bookId}/rating/{bookRatingId}")
    public ResponseEntity<Void> deleteBookRating(@PathVariable("bookId") Integer bookId, @PathVariable("bookRatingId") Integer bookRatingId) {
        bookService.deleteBookRating(bookId, bookRatingId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{bookId}/review/{bookReviewId}")
    public ResponseEntity<Void> deleteBookReview(@PathVariable("bookId") Integer bookId, @PathVariable("bookReviewId") Integer bookReviewId) {
        bookService.deleteBookReview(bookReviewId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-books")
    public ResponseEntity<Page<BookResponseDTO>> getDownloadedBooksByUsername(@AuthenticatedUsername String username,
                                                                              Pageable pageable) {
        Page<BookResponseDTO> downloadedBooksByUser = bookService.getDownloadedBooksByUsername(username, pageable)
                .map(bookMapper::toResponseDTO);
        return ResponseEntity.ok(downloadedBooksByUser);
    }
}
