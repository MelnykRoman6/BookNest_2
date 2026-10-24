package org.example.booknest_2.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "\"users\"")
@Getter
@Setter
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "nickname", unique = true)
    private String nickname;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "date_reg")
    private LocalDateTime registrationDate;

    @Column(name = "is_verified")
    private boolean isVerified;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Review> reviews;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private Set<Collection> collections;

    @ManyToMany
    @JoinTable(
            name = "user_save",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private Set<Book> savedBooks;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<SearchHistory> searchHistory;

    public LocalDateTime getDateReg() {
        return registrationDate;
    }

    public void setDateReg(LocalDateTime now) {
        registrationDate = now;
    }
}