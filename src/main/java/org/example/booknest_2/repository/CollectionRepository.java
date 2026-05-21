package org.example.booknest_2.repository;

import org.example.booknest_2.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByUsersEmail(String email);
}
