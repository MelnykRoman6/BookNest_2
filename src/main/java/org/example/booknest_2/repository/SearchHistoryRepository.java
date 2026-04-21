package org.example.booknest_2.repository;

import org.example.booknest_2.model.SearchHistory;
import org.example.booknest_2.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    Optional<SearchHistory> findByUserAndSearchCriteriaAndSearchDateAfter(
            Users user,
            String criteria,
            LocalDateTime date
    );
}