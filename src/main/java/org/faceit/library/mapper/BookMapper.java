package org.faceit.library.mapper;

import org.faceit.library.db.entity.Book;
import org.faceit.library.dto.request.BookRequestDTO;
import org.faceit.library.dto.response.BookResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface BookMapper {
    @Mapping(target = "averageRating", source = "avgRating")
    BookResponseDTO toResponseDTO(Book book);

    Book toEntity(BookRequestDTO bookRequestDTO);
}
