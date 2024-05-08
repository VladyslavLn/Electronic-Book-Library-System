package org.faceit.library.mapper;

import org.faceit.library.db.entity.Book;
import org.faceit.library.dto.request.BookRequestDTO;
import org.faceit.library.dto.response.BookResponseDTO;
import org.faceit.library.service.BookMapperHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {
        BookReviewMapper.class,
        BookRatingMapper.class,
        BookMapperHelper.class
})
public interface BookMapper {
    @Mapping(target = "averageRating", source = "avgRating")
    @Mapping(target = "cover", source = "bookCover", qualifiedByName = "mapCover")
    BookResponseDTO toResponseDTO(Book book);

    Book toEntity(BookRequestDTO bookRequestDTO);
}
