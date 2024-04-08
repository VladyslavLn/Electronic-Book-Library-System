package org.faceit.library.mapper;

import org.faceit.library.db.entity.BookReview;
import org.faceit.library.dto.response.BookReviewResponseDTO;
import org.mapstruct.Mapper;

@Mapper
public interface BookReviewMapper {
    BookReviewResponseDTO toDto(BookReview entity);
}
