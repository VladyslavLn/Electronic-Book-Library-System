package org.faceit.library.mapper;

import org.faceit.library.db.entity.BookRating;
import org.faceit.library.dto.response.BookRatingResponseDTO;
import org.mapstruct.Mapper;

@Mapper
public interface BookRatingMapper {
    BookRatingResponseDTO toDto(BookRating bookRating);
}
