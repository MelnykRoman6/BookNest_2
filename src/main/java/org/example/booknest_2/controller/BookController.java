//controller for all operations with books

package org.example.booknest_2.controller;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.LogManager;
import org.example.booknest_2.dto.CollectionDTO;
import org.example.booknest_2.model.Author;
import org.example.booknest_2.model.Book;
import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.*;
import org.example.booknest_2.service.BookService;
import org.example.booknest_2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class BookController {

    @Autowired
    private BookService bookService;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) Long author,
                        @RequestParam(required = false) Long genre,
                        Model model, @AuthenticationPrincipal UserDetails currentUser) {

        model.addAttribute("authors", authorRepository.findAll());
        model.addAttribute("genres", genreRepository.findAll());

        Users currentUserObj = null;
        String userEmail = null;
        if (currentUser != null) {
            userEmail = currentUser.getUsername();
            currentUserObj = usersRepository.findByEmail(userEmail);
        }

        if (search != null || author != null || genre != null) {
            List<Book> localBooks = bookService.searchLocalBooks(search, author, genre, currentUserObj);
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
            String username = currentUser.getUsername();
            Users user = null;
            log.info("BookContr debug: username: " + username);

            if (username.contains("@")) {
                System.out.println("userController debug: user is logged in with email: " + username);
                user = usersRepository.findByEmail(username);
            } else {
                System.out.println("userController debug: user is logged in with nickname: " + username);
                user = usersRepository.findByNickname(username);
            }

            String email = user.getEmail();
            log.info("BookContr debug: user email: " + email);

            List<CollectionDTO> userCollections = userService.getCollectionsByEmailorNickname(email);
            log.info("BookContr debug: collections found: " + (userCollections != null ? userCollections.size() : "null"));

            model.addAttribute("collections", userCollections);
        } else {
            log.info("BookContr debug: user not logged in");
        }

        return "book-details";
    }
    @GetMapping("/book/{id}/read")
    public String readBook(@PathVariable String id, Model model) {
        Book book = bookService.getOrSaveBookDetails(id, null);
        if (book == null) {
            return "error/404";
        }

        String iaId = bookService.fetchInternetArchiveId(book.getOpenLibraryId());

        model.addAttribute("book", book);
        model.addAttribute("iaId", iaId);

        String authorName = "Unknown Author";
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            authorName = book.getAuthors().stream()
                    .map(Author::getName)
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        model.addAttribute("authorName", authorName);

        return "reader";
    }
}
