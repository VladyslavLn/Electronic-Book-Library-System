package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.faceit.library.db.entity.Book;
import org.faceit.library.service.exception.CreateCoverException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service(value = "pdfBookCoverService")
@RequiredArgsConstructor
public class PdfBookCoverServiceImpl implements BookCoverService {
    @Value("${book.folder}")
    private String booksFolder;
    @Value("${book.cover.folder}")
    private String booksCoverFolder;

    @Override
    public String createBookCover(Book book) {
        String bookCoverFileKey = book.getFileKey().replaceFirst("[.][^.]+$", ".jpg");
        try (PDDocument document = PDDocument.load(new File(booksFolder + book.getFileKey()))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);
            File outputFile = new File(booksCoverFolder, bookCoverFileKey);
            outputFile.getParentFile().mkdirs();
            ImageIO.write(image, "JPEG", outputFile);
            return bookCoverFileKey;
        } catch (IOException e) {
            throw new CreateCoverException("Error processing book cover for: " + book.getFileKey(), e);
        }
    }
}
