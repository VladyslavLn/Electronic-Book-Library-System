package org.faceit.library.db.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class BookRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    private Book book;
    @ManyToOne
    private User user;
    @Column(name = "rating_value")
    private Integer ratingValue;
}
