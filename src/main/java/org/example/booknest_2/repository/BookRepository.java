package org.example.booknest_2.repository;

import org.example.booknest_2.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    @Query("SELECT b FROM Book b " +
            "LEFT JOIN b.authors a " +
            "LEFT JOIN b.genres g " +
            "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:authorId IS NULL OR a.id = :authorId) " +
            "AND (:genreId IS NULL OR g.id = :genreId)")
    List<Book> searchBooks(@Param("title") String title,
                           @Param("authorId") Long authorId,
                           @Param("genreId") Long genreId);
    List<Book> findByTitleContainingIgnoreCase(String title);
    Optional<Book> findByOpenLibraryId(String openLibraryId);
}