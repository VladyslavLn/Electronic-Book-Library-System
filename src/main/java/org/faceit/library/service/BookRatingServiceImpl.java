package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.faceit.library.db.entity.BookRating;
import org.faceit.library.db.repository.BookRatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class BookRatingServiceImpl implements BookRatingService {
    private final BookRatingRepository bookRatingRepository;

    @Override
    public BookRating saveBookRating(BookRating bookRating) {
        return bookRatingRepository.save(bookRating);
    }

    @Override
    public BookRating getBookRatingById(Integer id) {
        return bookRatingRepository.getReferenceById(id);
    }

    @Override
    public void deleteBookRating(Integer id) {
        bookRatingRepository.deleteById(id);
    }
}
