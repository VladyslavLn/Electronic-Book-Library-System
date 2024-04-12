package org.faceit.library.db.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

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
    @Column(name = "created_at")
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @Column(name = "changed_at")
    @UpdateTimestamp
    private OffsetDateTime changedAt;
}
