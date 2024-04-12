package org.faceit.library.service;

import org.faceit.library.db.entity.BookRating;

public interface BookRatingService {
    BookRating saveBookRating(BookRating bookRating);

    BookRating getBookRatingById(Integer id);

    void deleteBookRating(Integer id);
}
