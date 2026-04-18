package org.example.booknest_2.repository;

import org.example.booknest_2.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    Optional<Author> findByOpenLibraryAuthorId(String openLibraryAuthorId);
    // Здесь пока не нужно писать методы, findAll() уже есть внутри JpaRepository
}