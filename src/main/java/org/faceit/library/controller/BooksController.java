package org.faceit.library.controller;

import lombok.RequiredArgsConstructor;
import org.faceit.library.service.BookService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
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

    @GetMapping("/{userId}")
    public ResponseEntity<List<String>> getAvailableBooksForUser(@PathVariable Integer userId) {
        List<String> availableBooksList = bookService.getDownloadedBooksByUserId(userId);
        return ResponseEntity.ok(availableBooksList);
    }

    @PostMapping("/{userId}/upload")
    public ResponseEntity<String> uploadBook(@PathVariable Integer userId, @RequestPart MultipartFile file) {
        bookService.uploadBook(userId, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/download")
    public ResponseEntity<Object> downloadBook(@PathVariable Integer userId, @RequestParam String fileKey) {
        byte[] data = bookService.getBook(userId, fileKey);
        String contentType;
        if (fileKey.endsWith(".pdf")) {
            contentType = "application/pdf";
        } else if (fileKey.endsWith(".epub")) {
            contentType = "application/epub+zip";
        } else if (fileKey.endsWith(".fb2")) {
            contentType = "application/x-fictionbook+xml";
        } else {
            throw new IllegalStateException("Unsupported file type.");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileKey + "\"")
                .body(new ByteArrayResource(data));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer userId, @RequestParam String fileKey) {
        bookService.deleteBook(userId, fileKey);
        return ResponseEntity.ok().build();
    }
}
