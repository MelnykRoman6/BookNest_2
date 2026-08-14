package org.example.booknest_2.service;

import org.example.booknest_2.model.Book;
import org.example.booknest_2.model.Collection;
import org.example.booknest_2.repository.BookRepository;
import org.example.booknest_2.repository.CollectionRepository;
import org.example.booknest_2.util.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectionService {

    @Autowired
    private CollectionRepository collectionRepository;
    @Autowired
    private BookRepository bookRepository;

    @Transactional
    public void addBookToCollection(String bookId, Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(NotFoundException.of("Collection", collectionId));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(NotFoundException.of("Book", bookId));

        if (!collection.getBooks().contains(book)) {
            collection.getBooks().add(book);
            collectionRepository.save(collection);
        }
    }
}