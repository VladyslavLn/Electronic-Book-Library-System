package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.faceit.library.db.entity.Book;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfBookCoverServiceImpl implements BookCoverService {
    private static final String BOOKS_FOLDER = "books/";
    private static final String BOOKS_COVER_FOLDER = "books-cover/";

    @Override
    public String createBookCover(Book book) {
        String bookCoverFileKey = book.getFileKey().replaceFirst("[.][^.]+$", ".jpg");
        try (PDDocument document = PDDocument.load(new File(BOOKS_FOLDER + book.getFileKey()))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);
            File outputFile = new File(BOOKS_COVER_FOLDER, bookCoverFileKey);
            outputFile.getParentFile().mkdirs();
            ImageIO.write(image, "JPEG", outputFile);
            return bookCoverFileKey;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
