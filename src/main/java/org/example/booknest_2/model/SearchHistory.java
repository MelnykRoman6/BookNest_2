package org.example.booknest_2.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "history")
@Getter
@Setter
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "search_criteria")
    private String searchCriteria;

    @Column(name = "search_date")
    private LocalDateTime searchDate;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private Users user;
}