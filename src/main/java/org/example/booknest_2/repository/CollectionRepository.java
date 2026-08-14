package org.example.booknest_2.repository;

import org.example.booknest_2.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    @Query("SELECT DISTINCT c FROM Collection c LEFT JOIN FETCH c.books WHERE c.users.email = :email")
    List<Collection> findByUsersEmail(@Param("email") String email);

    @Query("SELECT c FROM Collection c WHERE c.id = :id AND c.users.email = :email")
    Optional<Collection> findOwnedByEmail(@Param("id") Long id, @Param("email") String email);
}
