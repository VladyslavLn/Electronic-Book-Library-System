package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import nl.siegmann.epublib.epub.EpubReader;
import org.faceit.library.db.entity.Book;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EpubBookCoverServiceImpl implements BookCoverService {
    private static final String BOOKS_FOLDER = "books/";
    private static final String BOOKS_COVER_FOLDER = "books-cover/";

    @Override
    public String createBookCover(Book book) {
        String bookCoverFileKey = book.getFileKey().replaceFirst("[.][^.]+$", ".jpg");
        nl.siegmann.epublib.domain.Book epubBook;
        try {
            epubBook = (new EpubReader()).readEpub(new FileInputStream(BOOKS_FOLDER + book.getFileKey()));
            BufferedImage coverImage = ImageIO.read(epubBook.getCoverImage().getInputStream());
            File outputFile = new File(BOOKS_COVER_FOLDER, bookCoverFileKey);
            outputFile.getParentFile().mkdirs();
            ImageIO.write(coverImage, "JPEG", outputFile);
            return bookCoverFileKey;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + BOOKS_FOLDER + book.getFileKey(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error processing book cover for: " + book.getFileKey(), e);
        }
    }
}
