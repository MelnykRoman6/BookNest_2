package org.example.booknest_2.dto;

import org.example.booknest_2.model.Book;
import org.example.booknest_2.model.Collection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionDTOTest {

    private Book book(long id, String title) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        return book;
    }

    @Test
    void mapsIdNameAndBooks() {
        Collection collection = new Collection();
        collection.setId(3);
        collection.setName("Favorites");
        collection.setBooks(List.of(book(1L, "Dune"), book(2L, "Neuromancer")));

        CollectionDTO dto = new CollectionDTO(collection);

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getName()).isEqualTo("Favorites");
        assertThat(dto.getTotalBooks()).isEqualTo(2);
        assertThat(dto.getBookTitles()).containsExactly("Dune", "Neuromancer");
        assertThat(dto.getBookIds()).containsExactly("1", "2");
    }

    @Test
    void leavesBookListsUnsetWhenCollectionHasNoBooks() {
        Collection collection = new Collection();
        collection.setName("Reading");
        collection.setBooks(null);

        CollectionDTO dto = new CollectionDTO(collection);

        assertThat(dto.getTotalBooks()).isZero();
        assertThat(dto.getBookTitles()).isNull();
        assertThat(dto.getBookIds()).isNull();
    }
}
