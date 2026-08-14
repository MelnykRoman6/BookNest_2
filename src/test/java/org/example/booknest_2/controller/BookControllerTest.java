package org.example.booknest_2.controller;

import org.example.booknest_2.dto.CollectionDTO;
import org.example.booknest_2.dto.ExternalBookDTO;
import org.example.booknest_2.model.Author;
import org.example.booknest_2.model.Book;
import org.example.booknest_2.model.Collection;
import org.example.booknest_2.model.Genre;
import org.example.booknest_2.repository.AuthorRepository;
import org.example.booknest_2.repository.BookRepository;
import org.example.booknest_2.repository.GenreRepository;
import org.example.booknest_2.service.BookService;
import org.example.booknest_2.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private BookController bookController;

    private UserDetails principal(String email) {
        return new User(email, "hashed", List.of());
    }

    private Book book(String openLibraryId, Set<Author> authors) {
        Book book = new Book();
        book.setOpenLibraryId(openLibraryId);
        book.setTitle("Dune");
        book.setAuthors(authors);
        return book;
    }

    private Author author(String name) {
        Author author = new Author();
        author.setName(name);
        return author;
    }

    @Test
    void indexWithoutFiltersOnlyExposesAuthorsAndGenres() {
        when(authorRepository.findAll()).thenReturn(List.of(author("Frank Herbert")));
        when(genreRepository.findAll()).thenReturn(List.of(new Genre()));
        Model model = new ConcurrentModel();

        String view = bookController.index(null, null, null, model);

        assertThat(view).isEqualTo("index");
        assertThat(model.asMap()).containsKeys("authors", "genres");
        assertThat(model.asMap()).doesNotContainKeys("localBooks", "externalBooks");
        verify(bookService, never()).searchLocalBooks(any(), any(), any(), any());
    }

    @Test
    void indexWithSearchAddsLocalAndExternalResults() {
        Book local = book("OL1W", null);
        ExternalBookDTO external = new ExternalBookDTO();
        when(bookService.searchLocalBooks("dune", null, null, null)).thenReturn(List.of(local));
        when(bookService.searchExternalBooks("dune")).thenReturn(List.of(external));
        Model model = new ConcurrentModel();

        bookController.index("dune", null, null, model);

        assertThat(model.getAttribute("localBooks")).isEqualTo(List.of(local));
        assertThat(model.getAttribute("externalBooks")).isEqualTo(List.of(external));
    }

    @Test
    void indexWithFilterOnlySkipsExternalSearch() {
        when(bookService.searchLocalBooks(null, 3L, null, null)).thenReturn(List.of());
        Model model = new ConcurrentModel();

        bookController.index(null, 3L, null, model);

        assertThat(model.asMap()).containsKey("localBooks");
        assertThat(model.asMap()).doesNotContainKey("externalBooks");
        verify(bookService, never()).searchExternalBooks(anyString());
    }

    @Test
    void showBookDetailsReturnsNotFoundViewWhenBookMissing() {
        when(bookService.getOrSaveBookDetails("OL1W", null)).thenReturn(null);

        String view = bookController.showBookDetails("OL1W", null, null, new ConcurrentModel(), null);

        assertThat(view).isEqualTo("error/404");
    }

    @Test
    void showBookDetailsAddsCollectionsForAuthenticatedUser() {
        Book found = book("OL1W", null);
        Collection collection = new Collection();
        collection.setName("Favorites");
        CollectionDTO dto = new CollectionDTO(collection);
        when(bookService.getOrSaveBookDetails("OL1W", "Frank Herbert")).thenReturn(found);
        when(userService.getCollectionsByEmail("reader@example.com")).thenReturn(List.of(dto));
        Model model = new ConcurrentModel();

        String view = bookController.showBookDetails("OL1W", "dune1965", "Frank Herbert", model,
                principal("reader@example.com"));

        assertThat(view).isEqualTo("book-details");
        assertThat(model.getAttribute("book")).isSameAs(found);
        assertThat(model.getAttribute("isLocal")).isEqualTo(true);
        assertThat(model.getAttribute("iaId")).isEqualTo("dune1965");
        assertThat(model.getAttribute("collections")).isEqualTo(List.of(dto));
    }

    @Test
    void showBookDetailsOmitsCollectionsForAnonymousUser() {
        when(bookService.getOrSaveBookDetails("OL1W", null)).thenReturn(book("OL1W", null));
        Model model = new ConcurrentModel();

        bookController.showBookDetails("OL1W", null, null, model, null);

        assertThat(model.asMap()).doesNotContainKey("collections");
        verify(userService, never()).getCollectionsByEmail(anyString());
    }

    @Test
    void readBookReturnsNotFoundViewWhenBookMissing() {
        when(bookService.getOrSaveBookDetails("OL1W", null)).thenReturn(null);

        assertThat(bookController.readBook("OL1W", new ConcurrentModel())).isEqualTo("error/404");
    }

    @Test
    void readBookJoinsAuthorNames() {
        Set<Author> authors = new LinkedHashSet<>(List.of(author("Frank Herbert")));
        when(bookService.getOrSaveBookDetails("OL1W", null)).thenReturn(book("OL1W", authors));
        when(bookService.fetchInternetArchiveId("OL1W")).thenReturn("dune1965");
        Model model = new ConcurrentModel();

        String view = bookController.readBook("OL1W", model);

        assertThat(view).isEqualTo("reader");
        assertThat(model.getAttribute("iaId")).isEqualTo("dune1965");
        assertThat(model.getAttribute("authorName")).isEqualTo("Frank Herbert");
    }

    @Test
    void readBookFallsBackToUnknownAuthor() {
        when(bookService.getOrSaveBookDetails("OL1W", null)).thenReturn(book("OL1W", Set.of()));
        when(bookService.fetchInternetArchiveId("OL1W")).thenReturn(null);
        Model model = new ConcurrentModel();

        bookController.readBook("OL1W", model);

        assertThat(model.getAttribute("authorName")).isEqualTo("Unknown Author");
    }
}
