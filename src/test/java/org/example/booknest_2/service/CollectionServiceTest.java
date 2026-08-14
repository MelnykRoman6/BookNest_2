package org.example.booknest_2.service;

import org.example.booknest_2.model.Book;
import org.example.booknest_2.model.Collection;
import org.example.booknest_2.repository.BookRepository;
import org.example.booknest_2.repository.CollectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CollectionService collectionService;

    private Book book(long id) {
        Book book = new Book();
        book.setId(id);
        book.setTitle("Dune");
        return book;
    }

    @Test
    void addBookToCollectionSavesNewBook() {
        Collection collection = new Collection();
        collection.setBooks(new ArrayList<>());
        Book book = book(1L);
        when(collectionRepository.findById(5L)).thenReturn(Optional.of(collection));
        when(bookRepository.findById("1")).thenReturn(Optional.of(book));

        collectionService.addBookToCollection("1", 5L);

        assertThat(collection.getBooks()).containsExactly(book);
        verify(collectionRepository).save(collection);
    }

    @Test
    void addBookToCollectionIgnoresDuplicates() {
        Book book = book(1L);
        Collection collection = new Collection();
        collection.setBooks(new ArrayList<>(List.of(book)));
        when(collectionRepository.findById(5L)).thenReturn(Optional.of(collection));
        when(bookRepository.findById("1")).thenReturn(Optional.of(book));

        collectionService.addBookToCollection("1", 5L);

        assertThat(collection.getBooks()).hasSize(1);
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void addBookToCollectionThrowsWhenCollectionMissing() {
        when(collectionRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.addBookToCollection("1", 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Collection not found");
    }

    @Test
    void addBookToCollectionThrowsWhenBookMissing() {
        when(collectionRepository.findById(5L)).thenReturn(Optional.of(new Collection()));
        when(bookRepository.findById("1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.addBookToCollection("1", 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Book not found");
    }
}
