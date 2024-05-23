package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.faceit.library.db.entity.BookRating;
import org.faceit.library.db.entity.BookReview;
import org.faceit.library.db.entity.User;
import org.faceit.library.dto.response.BookRatingResponseDTO;
import org.faceit.library.dto.response.BookReviewAndRatingResponseDTO;
import org.faceit.library.dto.response.BookReviewResponseDTO;
import org.faceit.library.mapper.BookRatingMapper;
import org.faceit.library.mapper.BookReviewMapper;
import org.faceit.library.mapper.UserMapper;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookMapperHelper {
    private final S3Service s3Service;
    private final BookReviewMapper reviewMapper;
    private final BookRatingMapper ratingMapper;
    private final UserMapper userMapper;

    @Named("mapCover")
    public byte[] mapCover(String bookCover) {
        if (StringUtils.isBlank(bookCover)) {
            return null;
        }
        return s3Service.getBookCover(bookCover);
    }

    @Named("mapToReviewAndRatings")
    public List<BookReviewAndRatingResponseDTO> mapToReviewAndRatings(
            List<BookReview> reviews, List<BookRating> ratings) {
        if (reviews == null || ratings == null) {
            return new ArrayList<>();
        }
        Map<User, BookReview> reviewsByUser = reviews.stream()
                .collect(Collectors.toMap(BookReview::getUser, Function.identity()));
        Map<User, BookRating> ratingsByUser = ratings.stream()
                .collect(Collectors.toMap(BookRating::getUser, Function.identity()));

        List<BookReviewAndRatingResponseDTO> result = new ArrayList<>();
        for (Map.Entry<User, BookReview> entry : reviewsByUser.entrySet()) {
            BookReviewAndRatingResponseDTO dto = new BookReviewAndRatingResponseDTO();
            User user = entry.getKey();

            dto.setUser(userMapper.toResponseDTO(user));

            BookReviewResponseDTO reviewDto = reviewMapper.toDto(entry.getValue());
            dto.setBookReview(reviewDto);

            BookRatingResponseDTO ratingDto = ratingMapper.toDto(ratingsByUser.get(user));
            dto.setBookRating(ratingDto);

            result.add(dto);
        }

        return result;
    }
}
