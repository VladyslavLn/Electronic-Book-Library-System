package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import nl.siegmann.epublib.epub.EpubReader;
import org.faceit.library.db.entity.Book;
import org.faceit.library.service.exception.CreateCoverException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service(value = "epubBookCoverService")
@RequiredArgsConstructor
public class EpubBookCoverServiceImpl implements BookCoverService {
    @Value("${book.folder}")
    private String booksFolder;
    @Value("${book.cover.folder}")
    private String booksCoverFolder;

    @Override
    public String createBookCover(Book book) {
        String bookCoverFileKey = book.getFileKey().replaceFirst("[.][^.]+$", ".jpg");
        nl.siegmann.epublib.domain.Book epubBook;
        try {
            epubBook = (new EpubReader()).readEpub(new FileInputStream(booksFolder + book.getFileKey()));
            BufferedImage coverImage = ImageIO.read(epubBook.getCoverImage().getInputStream());
            File outputFile = new File(booksCoverFolder, bookCoverFileKey);
            outputFile.getParentFile().mkdirs();
            ImageIO.write(coverImage, "JPEG", outputFile);
            return bookCoverFileKey;
        } catch (FileNotFoundException e) {
            throw new CreateCoverException("File not found: " + booksFolder + book.getFileKey(), e);
        } catch (IOException e) {
            throw new CreateCoverException("Error processing book cover for: " + book.getFileKey(), e);
        }
    }
}
