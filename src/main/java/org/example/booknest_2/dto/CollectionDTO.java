package org.example.booknest_2.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.booknest_2.model.Book;
import org.example.booknest_2.model.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CollectionDTO {
    @Setter
    private Long id;
    @Setter
    private String name;
    private int totalBooks;
    private List<String> bookIds;
    private List<String> bookTitles;

    public CollectionDTO(Collection entity) {
        this.id = (long) entity.getId();
        this.name = entity.getName();

        if (entity.getBooks() != null) {
            this.totalBooks = entity.getBooks().size();
            this.bookTitles = entity.getBooks().stream()
                    .map(Book::getTitle)
                    .collect(Collectors.toList());
            this.bookIds = entity.getBooks().stream()
                    .map(book -> String.valueOf(book.getId()))
                    .collect(Collectors.toList());
        }
    }
}