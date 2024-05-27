package org.faceit.library.service;

import org.faceit.library.db.entity.BookRating;
import org.faceit.library.db.entity.BookReview;
import org.faceit.library.dto.response.BookReviewAndRatingResponseDTO;
import org.faceit.library.mapper.BookRatingMapper;
import org.faceit.library.mapper.BookReviewMapper;
import org.faceit.library.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class BookMapperHelperTest {
    @MockBean
    private S3Service s3Service;
    @Autowired
    private BookReviewMapper reviewMapper;
    @Autowired
    private BookRatingMapper ratingMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BookMapperHelper bookMapperHelper;

    @Test
    void testMapCover() {
        String bookCover = "bookCover";
        byte[] cover = "cover".getBytes();

        when(s3Service.getBookCover(bookCover)).thenReturn(cover);

        byte[] result = bookMapperHelper.mapCover(bookCover);
        assertNotNull(result);
        assertEquals(cover, result);
    }

    @Test
    void testMapCover_returnNullWhenPassEmptyString() {
        String bookCover = " ";

        byte[] result = bookMapperHelper.mapCover(bookCover);
        assertNull(result);
    }

    @Test
    void testMapToReviewAndRatings() {
        String reviewContent = "reviewContent";
        List<BookReview> reviews = new ArrayList<>();
        BookReview review = new BookReview();
        review.setReviewContent(reviewContent);
        review.setId(1);
        reviews.add(review);
        List<BookRating> ratings = new ArrayList<>();
        BookRating bookRating = new BookRating();
        bookRating.setId(1);
        bookRating.setRatingValue(5);
        List<BookReviewAndRatingResponseDTO> result = bookMapperHelper.mapToReviewAndRatings(reviews, ratings);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testMapToReviewAndRatings_returnEmptyListWhenNoReviewsOrRatings() {
        List<BookReview> reviews = new ArrayList<>();
        List<BookRating> ratings = new ArrayList<>();
        List<BookReviewAndRatingResponseDTO> result = bookMapperHelper.mapToReviewAndRatings(null, ratings);
        assertNotNull(result);
        assertEquals(0, result.size());

        List<BookReviewAndRatingResponseDTO> result2 = bookMapperHelper.mapToReviewAndRatings(reviews, null);
        assertNotNull(result2);
        assertEquals(0, result2.size());
    }
}