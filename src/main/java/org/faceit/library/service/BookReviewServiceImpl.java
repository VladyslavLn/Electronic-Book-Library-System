package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.faceit.library.db.entity.BookReview;
import org.faceit.library.db.repository.BookReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class BookReviewServiceImpl implements BookReviewService {
    private final BookReviewRepository bookReviewRepository;

    @Override
    public BookReview createReview(BookReview bookReview) {
        return bookReviewRepository.save(bookReview);
    }

    @Override
    public Page<BookReview> getAllReviews(Pageable pageable) {
        return bookReviewRepository.findAll(pageable);
    }

    @Override
    public BookReview getReview(Integer id) {
        return bookReviewRepository.getReferenceById(id);
    }

    @Override
    public BookReview updateReview(BookReview bookReview) {
        return bookReviewRepository.save(bookReview);
    }

    @Override
    public void deleteReview(Integer id) {
        bookReviewRepository.deleteById(id);
    }
}
