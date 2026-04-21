package org.example.booknest_2.dto;

import org.example.booknest_2.model.Collection; // Убедись, что импорт правильный
import java.util.List;
import java.util.stream.Collectors;

public class CollectionDTO {
    private int id;
    private String name;
    private int totalBooks;
    private List<String> bookTitles;

    public CollectionDTO() {}

    public CollectionDTO(Collection entity) {
        this.id = entity.getId();
        this.name = entity.getName();

        if (entity.getBooks() != null) {
            this.totalBooks = entity.getBooks().size();
            this.bookTitles = entity.getBooks().stream()
                    .map(book -> book.getTitle())
                    .collect(Collectors.toList());
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalBooks() { return totalBooks; }
    public void setTotalBooks(int totalBooks) { this.totalBooks = totalBooks; }

    public List<String> getBookTitles() { return bookTitles; }
    public void setBookTitles(List<String> bookTitles) { this.bookTitles = bookTitles; }
}