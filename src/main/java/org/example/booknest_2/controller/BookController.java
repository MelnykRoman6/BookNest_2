package org.example.booknest_2.controller;

import org.example.booknest_2.model.Book;
import org.example.booknest_2.repository.*;
import org.example.booknest_2.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/")
    public String index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) Long author,
                        @RequestParam(required = false) Long genre,
                        Model model) {

        // Передаем списки для выпадающих меню (Author/Genre)
        model.addAttribute("authors", authorRepository.findAll());
        model.addAttribute("genres", genreRepository.findAll());

        // Получаем книги из БД
        if (search != null || author != null || genre != null) {
            List<Book> localBooks = bookService.searchLocalBooks(search, author, genre, null);
            model.addAttribute("localBooks", localBooks);

            // Если ищем по названию, тянем из API
            if (search != null && !search.isEmpty()) {
                model.addAttribute("externalBooks", bookService.searchExternalBooks(search));
            }
        }

        return "index"; // Ищет файл src/main/resources/templates/index.html
    }
    @GetMapping("/book/{id}")
    public String showBookDetails(@PathVariable String id,
                                  @RequestParam(required = false) String ia,
                                  @RequestParam(value = "authorName", required = false) String authorName,
                                  Model model) {

        // 1. Ищем в базе: либо по нашему ID (Long), либо по OpenLibrary ID (String)
        Optional<Book> localBook = bookRepository.findByOpenLibraryId(id);
        System.out.println("DEBUG: Clicked on ID: " + id);
        System.out.println("DEBUG: Received Author Name: " + authorName);
        // Если по OL ID не нашли, попробуем по обычному Long ID (на случай, если это старая книга)
        if (localBook.isEmpty()) {
            try {
                localBook = bookRepository.findById(Long.parseLong(id));
            } catch (NumberFormatException ignored) {}
        }

        // 2. Если книга нашлась в базе — просто отдаем её
        if (localBook.isPresent()) {
            model.addAttribute("book", localBook.get());
            model.addAttribute("isLocal", true);
            model.addAttribute("iaId", ia);
            return "book-details";
        }
        System.out.println("CONTROLLER RECEIVED: " + authorName);
        Book savedBook = bookService.saveExternalBook(id, authorName);

        if (savedBook != null) {
            model.addAttribute("book", savedBook);
            model.addAttribute("isLocal", true); // Теперь она локальная!
        } else {
            // Если API не ответило, можно показать страницу ошибки или заглушку
            return "error/404";
        }

        model.addAttribute("iaId", ia);
        return "book-details";
    }
}
