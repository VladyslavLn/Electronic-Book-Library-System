package org.faceit.library.dto.response;

import lombok.Data;

@Data
public class BookReviewAndRatingResponseDTO {
    private UserResponseDTO user;
    private BookReviewResponseDTO bookReview;
    private BookRatingResponseDTO bookRating;
}
