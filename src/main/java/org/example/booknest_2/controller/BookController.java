//controller for all operations with books

package org.example.booknest_2.controller;

import org.example.booknest_2.dto.CollectionDTO;
import org.example.booknest_2.model.Book;
import org.example.booknest_2.repository.*;
import org.example.booknest_2.service.BookService;
import org.example.booknest_2.service.CollectionService;
import org.example.booknest_2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) Long author,
                        @RequestParam(required = false) Long genre,
                        Model model) {

        model.addAttribute("authors", authorRepository.findAll());
        model.addAttribute("genres", genreRepository.findAll());

        if (search != null || author != null || genre != null) {
            List<Book> localBooks = bookService.searchLocalBooks(search, author, genre, null);
            model.addAttribute("localBooks", localBooks);

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

        Book bookToShow = bookService.getOrSaveBookDetails(id, authorName);

        if (bookToShow == null) {
            return "error/404";
        }

        model.addAttribute("book", bookToShow);
        model.addAttribute("isLocal", true);
        model.addAttribute("iaId", ia);

        if (currentUser != null) {
            String email = currentUser.getUsername();
            System.out.println("BookContr debug: user mail: " + email);

            List<CollectionDTO> userCollections = userService.getCollectionsByEmail(email);
            System.out.println("BookContr debug: collections found: " + (userCollections != null ? userCollections.size() : "null"));

            model.addAttribute("collections", userCollections);
        } else {
            System.out.println("BookContr debug: user not logged in");
        }

        return "book-details";
    }
}
