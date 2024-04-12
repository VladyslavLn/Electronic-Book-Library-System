package org.faceit.library.service;

import org.faceit.library.db.entity.BookReview;

public interface BookReviewService {
    BookReview saveBookReview(BookReview bookReview);

    BookReview getBookReview(Integer id);

    void deleteBookReview(Integer id);
}
