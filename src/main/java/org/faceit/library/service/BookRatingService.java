package org.faceit.library.service;

import org.faceit.library.db.entity.BookRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRatingService {
    BookRating createBookRating(BookRating bookRating);

    Page<BookRating> getAllBookRatings(Pageable pageable);

    BookRating updateBookRating(BookRating bookRating);

    BookRating getBookRatingById(Integer id);

    void deleteBookRating(Integer id);
}
