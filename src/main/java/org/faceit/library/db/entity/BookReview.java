package org.faceit.library.db.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class BookReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    private Book book;
    @ManyToOne
    private User user;
    @Column(name = "review_content")
    private String reviewContent;
}
