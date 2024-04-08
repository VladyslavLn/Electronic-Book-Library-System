package org.faceit.library.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BookResponseDTO {
    private Integer id;
    private String title;
    private String author;
    private String language;
    private String fileKey;
    private List<BookReviewResponseDTO> reviews;
    private List<BookRatingResponseDTO> ratings;
    private Double averageRating;
}
