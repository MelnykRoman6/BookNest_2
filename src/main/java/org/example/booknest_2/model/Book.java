package org.example.booknest_2.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Entity
@Table(name = "book")
@Getter
@Setter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String language;

    @Column(name = "ia_id")
    private String iaId;

    @Column(name = "open_library_id")
    private String openLibraryId;

    @Column(name = "cover_id")
    private String coverId;

    @ManyToMany
    @JoinTable(
            name = "belong",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres;

    @ManyToMany
    @JoinTable(
            name = "`write`",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private Set<Review> reviews;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private Set<Format> formats;

    public String getCoverUrl() {
        if (this.coverId != null && !this.coverId.isEmpty()) {
            return "https://covers.openlibrary.org/b/id/" + this.coverId + "-M.jpg";
        }
        return "https://via.placeholder.com/100x150?text=No+Cover";
    }
}