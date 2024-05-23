package org.faceit.library.db.repository;

import org.faceit.library.db.entity.Book;
import org.faceit.library.db.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    Page<Book> getBooksByCreatedBy(User user, Pageable pageable);
}
