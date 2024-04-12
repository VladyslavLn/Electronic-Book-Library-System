package org.faceit.library.db.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String author;
    private String language;
    @Column(name = "file_key")
    private String fileKey;
    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    private List<BookReview> reviews;
    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    private List<BookRating> ratings;
    @Column(name = "avg_rating")
    @Min(value = 0)
    @Max(value = 5)
    private Double avgRating;
    @Column(name = "created_at")
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @Column(name = "changed_at")
    @UpdateTimestamp
    private OffsetDateTime changedAt;
}
