package org.example.booknest_2.controller;

import org.example.booknest_2.dto.CollectionDTO;
import org.example.booknest_2.model.Book;
import org.example.booknest_2.repository.*;
import org.example.booknest_2.service.BookService;
import org.example.booknest_2.service.CollectionService;
import org.example.booknest_2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private UserService userService;

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

        return "index";
    }
    @GetMapping("/book/{id}")
    public String showBookDetails(@PathVariable String id,
                                  @RequestParam(required = false) String ia,
                                  @RequestParam(value = "authorName", required = false) String authorName,
                                  Model model, @AuthenticationPrincipal UserDetails currentUser) {

        // 1. Ищем в базе: либо по OpenLibrary ID, либо по внутреннему ID
        Optional<Book> localBook = bookRepository.findByOpenLibraryId(id);
        System.out.println("DEBUG: Clicked on ID: " + id);
        System.out.println("DEBUG: Received Author Name: " + authorName);

        if (localBook.isEmpty()) {
            try {
                localBook = bookRepository.findByOpenLibraryId(id);
            } catch (NumberFormatException ignored) {}
        }

        Book bookToShow;

        if (localBook.isPresent()) {
            bookToShow = localBook.get();
            model.addAttribute("isLocal", true);
        } else {
            System.out.println("CONTROLLER RECEIVED EXTERNAL: " + authorName);
            bookToShow = bookService.saveExternalBook(id, authorName);
            if (bookToShow != null) {
                model.addAttribute("isLocal", true);
            } else {
                return "error/404";
            }
        }

        // Передаем книгу в модель один раз для обоих случаев
        model.addAttribute("book", bookToShow);
        model.addAttribute("iaId", ia);

        // 2. ВЫПОЛНЯЕТСЯ ВСЕГДА: Ищем коллекции, если пользователь залогинен
        if (currentUser != null) {
            String email = currentUser.getUsername();
            System.out.println("Залогинен юзер: " + email);

            // Вызываем у экземпляра сервиса (не забудь заинжектить userService через @Autowired вверху класса)
            List<CollectionDTO> userCollections = userService.getCollectionsByEmail(email);
            System.out.println("Найдено коллекций: " + (userCollections != null ? userCollections.size() : "null"));

            model.addAttribute("collections", userCollections);
        } else {
            System.out.println("Пользователь не залогинен");
        }

        return "book-details";
    }
}
