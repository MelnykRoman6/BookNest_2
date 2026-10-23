package org.example.booknest_2.repository;

import org.example.booknest_2.model.SearchHistory;
import org.example.booknest_2.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    @Query("SELECT DISTINCT s FROM SearchHistory s WHERE s.user.email = :email")
    List<SearchHistory> findByUsersEmail(@Param("email") String email);

    Optional<SearchHistory> findByUserAndSearchCriteriaAndSearchDateAfter(
            Users user,
            String criteria,
            LocalDateTime date
    );
}