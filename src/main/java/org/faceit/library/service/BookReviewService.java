package org.faceit.library.service;

import org.faceit.library.db.entity.BookReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookReviewService {
    BookReview createReview(BookReview bookReview);

    Page<BookReview> getAllReviews(Pageable pageable);

    BookReview getReview(Integer id);

    BookReview updateReview(BookReview bookReview);

    void deleteReview(Integer id);
}
