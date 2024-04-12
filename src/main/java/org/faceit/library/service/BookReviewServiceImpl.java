package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.faceit.library.db.entity.BookReview;
import org.faceit.library.db.repository.BookReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class BookReviewServiceImpl implements BookReviewService {
    private final BookReviewRepository bookReviewRepository;

    @Override
    public BookReview saveBookReview(BookReview bookReview) {
        return bookReviewRepository.save(bookReview);
    }

    @Override
    public BookReview getBookReview(Integer id) {
        return bookReviewRepository.getReferenceById(id);
    }

    @Override
    public void deleteBookReview(Integer id) {
        bookReviewRepository.deleteById(id);
    }
}
