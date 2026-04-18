package org.example.booknest_2.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Entity
@Table(name = "collection")
@Getter
@Setter
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "add",
            joinColumns = @JoinColumn(name = "id_collection"),
            inverseJoinColumns = @JoinColumn(name = "id_book")
    )
    private Set<Book> books;
}