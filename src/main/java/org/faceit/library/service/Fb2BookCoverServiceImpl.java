package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.faceit.library.db.entity.Book;
import org.faceit.library.service.exception.CreateCoverException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Service(value = "fb2BookCoverService")
@RequiredArgsConstructor
public class Fb2BookCoverServiceImpl implements BookCoverService {
    @Value("${book.folder}")
    private String booksFolder;
    @Value("${book.cover.folder}")
    private String booksCoverFolder;

    @Override
    public String createBookCover(Book book) {
        String bookCoverFileKey = book.getFileKey().replaceFirst("[.][^.]+$", ".jpg");
        try {
            File inputFile = new File(booksFolder + book.getFileKey());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList binaryDataNodes = doc.getElementsByTagName("binary");
            for (int i = 0; i < binaryDataNodes.getLength(); i++) {
                Node binaryNode = binaryDataNodes.item(i);

                if (binaryNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element binaryElement = (Element) binaryNode;
                    String id = binaryElement.getAttribute("id");
                    if (StringUtils.isNotBlank(id)) {
                        String coverData = binaryElement.getTextContent();
                        byte[] bytes = Base64.getMimeDecoder().decode(coverData);
                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                        File outputFile = new File(booksCoverFolder, bookCoverFileKey);
                        outputFile.getParentFile().mkdirs();
                        ImageIO.write(img, "JPEG", outputFile);
                        break;
                    }
                }
            }
            return bookCoverFileKey;
        } catch (IOException e) {
            throw new CreateCoverException("I/O error while processing the book cover for: " + book.getFileKey(), e);
        } catch (Exception e) {
            throw new CreateCoverException("General error when trying to create a book cover for: " + book.getFileKey(), e);
        }
    }
}
