package org.faceit.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.annotation.Before;
import org.faceit.library.JwtUtil;
import org.faceit.library.db.entity.Book;
import org.faceit.library.db.entity.BookRating;
import org.faceit.library.db.entity.BookReview;
import org.faceit.library.db.entity.User;
import org.faceit.library.db.repository.UserRepository;
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
import org.faceit.library.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BooksControllerTest {
    @Value("${api.prefix}")
    private String apiPrefix;
    @MockBean
    private BookService bookService;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private BookReviewMapper bookReviewMapper;
    @Autowired
    private BookRatingMapper bookRatingMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private S3Service s3Service;

    @BeforeEach
    void init() {
        when(s3Service.getObject(any())).thenReturn(new byte[0]);
    }

    @Test
    void testCreateBook() throws Exception {
        Book book = createBook();
        String token = JwtUtil.createToken(book.getCreatedBy().getEmail());
        BookRequestDTO requestDTO = new BookRequestDTO();
        requestDTO.setTitle(book.getTitle());
        requestDTO.setAuthor(book.getAuthor());
        requestDTO.setLanguage(book.getLanguage());
        MockMultipartFile bookFile = new MockMultipartFile("file", "book.pdf", "application/pdf", new byte[0]);
        MockMultipartFile bookJson = new MockMultipartFile("book", "", "application/json", objectMapper.writeValueAsString(requestDTO).getBytes());

        when(userRepository.findByEmail(book.getCreatedBy().getEmail())).thenReturn(Optional.of(book.getCreatedBy()));
        when(bookService.createBook(eq(book.getCreatedBy().getEmail()), any(), eq(bookFile))).thenReturn(book);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.multipart(apiPrefix + "/books")
                        .file(bookFile)
                        .file(bookJson)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn();
        BookResponseDTO bookResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), BookResponseDTO.class);
        assertNotNull(bookResponseDTO);
        assertEquals(book.getTitle(), bookResponseDTO.getTitle());
        assertEquals(book.getAuthor(), bookResponseDTO.getAuthor());
        assertEquals(book.getLanguage(), bookResponseDTO.getLanguage());
    }

    @Test
    @WithMockUser
    void getAllBooks() throws Exception {
        Book book = createBook();
        Page<Book> books = new PageImpl<>(List.of(book));

        when(bookService.getAllBooks(any())).thenReturn(books);

        mockMvc.perform(get(apiPrefix + "/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(books.getTotalElements()))
                .andExpect(jsonPath("$.content[0].title").value(book.getTitle()))
                .andExpect(jsonPath("$.content[0].author").value(book.getAuthor()))
                .andExpect(jsonPath("$.content[0].language").value(book.getLanguage()))
                .andExpect(jsonPath("$.content[0].id").value(book.getId()));
    }

    @Test
    void testUpdateBook() throws Exception {
        Book book = createBook();
        String token = JwtUtil.createToken(book.getCreatedBy().getEmail());
        BookRequestDTO requestDTO = new BookRequestDTO();
        requestDTO.setTitle(book.getTitle());
        requestDTO.setAuthor(book.getAuthor());
        requestDTO.setLanguage(book.getLanguage());

        when(userRepository.findByEmail(book.getCreatedBy().getEmail())).thenReturn(Optional.of(book.getCreatedBy()));
        when(bookService.updateBook(book.getId(), requestDTO, book.getCreatedBy().getEmail())).thenReturn(book);
        MvcResult mvcResult = mockMvc.perform(put(apiPrefix + "/books/{bookId}", book.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        BookResponseDTO bookResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), BookResponseDTO.class);
        assertNotNull(bookResponseDTO);
        assertEquals(book.getTitle(), bookResponseDTO.getTitle());
        assertEquals(book.getAuthor(), bookResponseDTO.getAuthor());
        assertEquals(book.getLanguage(), bookResponseDTO.getLanguage());
    }

    @Test
    @WithMockUser
    void testGetBookById() throws Exception {
        Book book = createBook();

        when(bookService.getBook(book.getId())).thenReturn(book);

        MvcResult mvcResult = mockMvc.perform(get(apiPrefix + "/books/{bookId}", book.getId()))
                .andExpect(status().isOk())
                .andReturn();
        BookResponseDTO bookResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), BookResponseDTO.class);
        assertNotNull(bookResponseDTO);
        assertEquals(book.getTitle(), bookResponseDTO.getTitle());
        assertEquals(book.getAuthor(), bookResponseDTO.getAuthor());
        assertEquals(book.getLanguage(), bookResponseDTO.getLanguage());
    }

    @Test
    @WithMockUser
    void testDeleteBook() throws Exception {
        doNothing().when(bookService).deleteBook(1);

        mockMvc.perform(delete(apiPrefix + "/books/{bookId}", 1))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testDownloadBook() throws Exception {
        Book book = createBook();
        BookFileMetadata data = new BookFileMetadata();
        data.setFileName(book.getFileKey());
        data.setFileData(new byte[0]);

        when(bookService.downloadBookFile(book.getId())).thenReturn(data);

        mockMvc.perform(get(apiPrefix + "/books/{bookId}/file/download", book.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + book.getFileKey() + "\""));
    }

    @Test
    void testAddReviewToBook() throws Exception {
        Book book = createBook();
        String token = JwtUtil.createToken(book.getCreatedBy().getEmail());
        BookReview bookReview = book.getReviews().get(0);
        BookReviewRequestDTO bookReviewRequestDTO = new BookReviewRequestDTO();
        bookReviewRequestDTO.setContent(bookReview.getReviewContent());

        when(userRepository.findByEmail(book.getCreatedBy().getEmail())).thenReturn(Optional.of(book.getCreatedBy()));
        when(bookService.addReviewToBook(book.getCreatedBy().getEmail(), book.getId(), bookReviewRequestDTO)).thenReturn(bookReview);

        MvcResult mvcResult = mockMvc.perform(post(apiPrefix + "/books/{bookId}/review", book.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(bookReviewRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookReviewResponseDTO bookReviewResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), BookReviewResponseDTO.class);
        assertNotNull(bookReviewResponseDTO);
        assertEquals(bookReviewRequestDTO.getContent(), bookReviewResponseDTO.getReviewContent());
    }

    @Test
    void testAddRatingToBook() throws Exception {
        Book book = createBook();
        String token = JwtUtil.createToken(book.getCreatedBy().getEmail());
        BookRating bookRating = book.getRatings().get(0);
        BookRatingRequestDTO bookRatingRequestDTO = new BookRatingRequestDTO();
        bookRatingRequestDTO.setRatingValue(5);

        when(userRepository.findByEmail(book.getCreatedBy().getEmail())).thenReturn(Optional.of(book.getCreatedBy()));
        when(bookService.addRatingToBook(book.getCreatedBy().getEmail(), book.getId(), bookRatingRequestDTO)).thenReturn(bookRating);

        MvcResult mvcResult = mockMvc.perform(post(apiPrefix + "/books/{bookId}/rating", book.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(bookRatingRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookRatingResponseDTO bookReviewResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), BookRatingResponseDTO.class);
        assertNotNull(bookReviewResponseDTO);
        assertEquals(bookRatingRequestDTO.getRatingValue(), bookReviewResponseDTO.getRatingValue());
    }

    @Test
    @WithMockUser
    void testDeleteBookRating() throws Exception {
        Book book = createBook();
        BookRating bookRating = book.getRatings().get(0);

        doNothing().when(bookService).deleteBookRating(book.getId(), bookRating.getId());

        mockMvc.perform(delete(apiPrefix + "/books/{bookId}/rating/{ratingId}", book.getId(), bookRating.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testDeleteBookReview() throws Exception {
        Book book = createBook();
        BookReview bookReview = book.getReviews().get(0);

        doNothing().when(bookService).deleteBookReview(bookReview.getId());

        mockMvc.perform(delete(apiPrefix + "/books/{bookId}/review/{reviewId}", book.getId(), bookReview.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void testGetDownloadedBooksByUsername() throws Exception {
        Book book = createBook();
        String token = JwtUtil.createToken(book.getCreatedBy().getEmail());
        Page<Book> books = new PageImpl<>(List.of(book));

        when(userRepository.findByEmail(book.getCreatedBy().getEmail())).thenReturn(Optional.of(book.getCreatedBy()));
        when(bookService.getDownloadedBooksByUserEmail(any(), any())).thenReturn(books);

        mockMvc.perform(get(apiPrefix + "/books/my-books")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(books.getTotalElements()))
                .andExpect(jsonPath("$.content[0].title").value(book.getTitle()))
                .andExpect(jsonPath("$.content[0].author").value(book.getAuthor()))
                .andExpect(jsonPath("$.content[0].language").value(book.getLanguage()))
                .andExpect(jsonPath("$.content[0].id").value(book.getId()));
    }

    private Book createBook() {
        Book book = new Book();
        book.setId(1);
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setLanguage("English");
        book.setFileKey("filekey.pdf");
        book.setBookCover("boookCover");
        BookReview bookReview = new BookReview();
        bookReview.setBook(book);
        bookReview.setReviewContent("reviewContent");
        bookReview.setId(1);
        book.setReviews(List.of(bookReview));
        BookRating bookRating = new BookRating();
        bookRating.setBook(book);
        bookRating.setId(1);
        bookRating.setRatingValue(5);
        book.setRatings(List.of(bookRating));
        book.setAvgRating(5.0);
        User user = new User();
        user.setId(1);
        user.setEmail("email@email.com");
        user.setFirstName("FirstName");
        user.setLastName("LastName");
        user.setRole(Collections.emptySet());
        book.setCreatedBy(user);
        return book;
    }
}