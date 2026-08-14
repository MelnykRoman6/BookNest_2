package org.example.booknest_2.service;

import org.example.booknest_2.dto.ExternalBookDTO;
import org.example.booknest_2.model.Author;
import org.example.booknest_2.model.Book;
import org.example.booknest_2.model.SearchHistory;
import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.AuthorRepository;
import org.example.booknest_2.repository.BookRepository;
import org.example.booknest_2.repository.SearchHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private SearchHistoryRepository searchHistoryRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    void injectRestTemplate() {
        ReflectionTestUtils.setField(bookService, "restTemplate", restTemplate);
    }

    private Users user(String email) {
        Users user = new Users();
        user.setEmail(email);
        return user;
    }

    @Test
    void searchLocalBooksSavesHistoryForLoggedInUserWithTitle() {
        Users currentUser = user("reader@example.com");
        Book book = new Book();
        when(searchHistoryRepository.findByUserAndSearchCriteriaAndSearchDateAfter(eq(currentUser), eq("dune"), any()))
                .thenReturn(Optional.empty());
        when(bookRepository.searchBooks("dune", 1L, 2L)).thenReturn(List.of(book));

        List<Book> result = bookService.searchLocalBooks("dune", 1L, 2L, currentUser);

        assertThat(result).containsExactly(book);
        ArgumentCaptor<SearchHistory> captor = ArgumentCaptor.forClass(SearchHistory.class);
        verify(searchHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getSearchCriteria()).isEqualTo("dune");
        assertThat(captor.getValue().getUser()).isSameAs(currentUser);
        assertThat(captor.getValue().getSearchDate()).isNotNull();
    }

    @Test
    void searchLocalBooksDoesNotDuplicateSameDayHistory() {
        Users currentUser = user("reader@example.com");
        when(searchHistoryRepository.findByUserAndSearchCriteriaAndSearchDateAfter(eq(currentUser), eq("dune"), any()))
                .thenReturn(Optional.of(new SearchHistory()));
        when(bookRepository.searchBooks("dune", null, null)).thenReturn(List.of());

        bookService.searchLocalBooks("dune", null, null, currentUser);

        verify(searchHistoryRepository, never()).save(any());
    }

    @Test
    void searchLocalBooksSkipsHistoryForAnonymousUserOrBlankTitle() {
        when(bookRepository.searchBooks(any(), any(), any())).thenReturn(List.of());

        bookService.searchLocalBooks("dune", null, null, null);
        bookService.searchLocalBooks(null, null, null, user("a@b.c"));
        bookService.searchLocalBooks("", null, null, user("a@b.c"));

        verify(searchHistoryRepository, never()).save(any());
        verify(bookRepository, never()).findByOpenLibraryId(anyString());
    }

    @Test
    void searchExternalBooksMapsDocsAndCapsResultsAtTwelve() {
        List<Map<String, Object>> docs = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("title", "Title " + i);
            doc.put("author_name", List.of("Author " + i));
            doc.put("key", "/works/OL" + i + "W");
            doc.put("cover_i", i);
            docs.add(doc);
        }
        when(restTemplate.getForObject(contains("q=the+hobbit"), eq(Map.class)))
                .thenReturn(Map.of("docs", docs));

        List<ExternalBookDTO> results = bookService.searchExternalBooks("the hobbit");

        assertThat(results).hasSize(12);
        assertThat(results.get(0).getTitle()).isEqualTo("Title 0");
        assertThat(results.get(0).getAuthorName()).isEqualTo("Author 0");
        assertThat(results.get(0).getOpenLibraryId()).isEqualTo("OL0W");
        assertThat(results.get(0).getCoverId()).isEqualTo("0");
    }

    @Test
    void searchExternalBooksUsesFallbacksForMissingFields() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("title", "No Metadata");
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(Map.of("docs", List.of(doc)));

        List<ExternalBookDTO> results = bookService.searchExternalBooks("query");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAuthorName()).isEqualTo("Unknown");
        assertThat(results.get(0).getOpenLibraryId()).isEmpty();
        assertThat(results.get(0).getCoverId()).isNull();
    }

    @Test
    void searchExternalBooksReturnsEmptyListWhenDocsMissingOrRequestFails() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(new HashMap<>())
                .thenThrow(new RestClientException("boom"));

        assertThat(bookService.searchExternalBooks("query")).isEmpty();
        assertThat(bookService.searchExternalBooks("query")).isEmpty();
    }

    @Test
    void getExternalBookDetailsFlattensDescriptionAndCover() {
        Map<String, Object> details = new HashMap<>();
        details.put("title", "Dune");
        details.put("description", Map.of("value", "A desert planet"));
        details.put("covers", List.of(42, 43));
        when(restTemplate.getForObject(contains("/works/OL1W.json"), eq(Map.class))).thenReturn(details);

        Map<String, Object> result = bookService.getExternalBookDetails("OL1W");

        assertThat(result.get("description")).isEqualTo("A desert planet");
        assertThat(result.get("cover_id")).isEqualTo("42");
    }

    @Test
    void getExternalBookDetailsReturnsNullOnFailure() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RestClientException("boom"));

        assertThat(bookService.getExternalBookDetails("OL1W")).isNull();
    }

    @Test
    void getOrSaveBookDetailsReturnsLocalBookWhenPresent() {
        Book local = new Book();
        when(bookRepository.findByOpenLibraryId("OL1W")).thenReturn(Optional.of(local));

        assertThat(bookService.getOrSaveBookDetails("OL1W", "Frank Herbert")).isSameAs(local);
        verify(restTemplate, never()).getForObject(anyString(), eq(Map.class));
    }

    @Test
    void getOrSaveBookDetailsFallsBackToExternalSave() {
        when(bookRepository.findByOpenLibraryId("OL1W")).thenReturn(Optional.empty());
        Map<String, Object> details = new HashMap<>();
        details.put("title", "Dune");
        when(restTemplate.getForObject(contains("OL1W.json"), eq(Map.class))).thenReturn(details);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book saved = bookService.getOrSaveBookDetails("OL1W", "Frank Herbert");

        assertThat(saved.getTitle()).isEqualTo("Dune");
        assertThat(saved.getOpenLibraryId()).isEqualTo("OL1W");
    }

    @Test
    void saveExternalBookReturnsExistingBookWithoutRemoteCall() {
        Book existing = new Book();
        when(bookRepository.findByOpenLibraryId("OL1W")).thenReturn(Optional.of(existing));

        assertThat(bookService.saveExternalBook("OL1W", "Frank Herbert")).isSameAs(existing);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void saveExternalBookReturnsNullWhenDetailsUnavailable() {
        when(bookRepository.findByOpenLibraryId("OL1W")).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RestClientException("boom"));

        assertThat(bookService.saveExternalBook("OL1W", "Frank Herbert")).isNull();
    }

    @Test
    void saveExternalBookPersistsAuthorsDescriptionAndCover() {
        when(bookRepository.findByOpenLibraryId("OL1W")).thenReturn(Optional.empty());
        Map<String, Object> details = new HashMap<>();
        details.put("title", "Dune");
        details.put("description", "A desert planet");
        details.put("covers", List.of(42));
        details.put("authors", List.of(Map.of("author", Map.of("key", "/authors/OL1A"))));
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(details);
        when(authorRepository.findByOpenLibraryAuthorId("OL1A")).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book saved = bookService.saveExternalBook("OL1W", "Frank Herbert");

        assertThat(saved.getDescription()).isEqualTo("A desert planet");
        assertThat(saved.getCoverId()).isEqualTo("42");
        assertThat(saved.getAuthors()).hasSize(1);
        Author author = saved.getAuthors().iterator().next();
        assertThat(author.getOpenLibraryAuthorId()).isEqualTo("OL1A");
        assertThat(author.getName()).isEqualTo("Frank Herbert");
    }

    @Test
    void saveExternalBookReusesKnownAuthorAndDefaultsMissingName() {
        when(bookRepository.findByOpenLibraryId("OL1W")).thenReturn(Optional.empty());
        Map<String, Object> details = new HashMap<>();
        details.put("title", "Dune");
        details.put("authors", List.of(Map.of("author", Map.of("key", "/authors/OL1A"))));
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(details);
        Author known = new Author();
        known.setName("Existing Author");
        when(authorRepository.findByOpenLibraryAuthorId("OL1A")).thenReturn(Optional.of(known));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book saved = bookService.saveExternalBook("OL1W", null);

        assertThat(saved.getAuthors()).containsExactly(known);
        verify(authorRepository, never()).save(any());
    }

    @Test
    void downloadAndSaveBookTextReturnsCachedContent() {
        Book book = new Book();
        book.setFileContent("cached text");

        assertThat(bookService.downloadAndSaveBookText(book)).isEqualTo("cached text");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void downloadAndSaveBookTextReturnsNullWhenNoArchiveId() {
        Book book = new Book();
        book.setOpenLibraryId("OL1W");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{}");

        assertThat(bookService.downloadAndSaveBookText(book)).isNull();
        verify(bookRepository, never()).save(any());
    }

    @Test
    void downloadAndSaveBookTextStoresFetchedText() {
        Book book = new Book();
        book.setOpenLibraryId("OL1W");
        when(restTemplate.getForObject(contains("OL1W.json"), eq(String.class)))
                .thenReturn("{\"ocaid\":\"dune1965\"}");
        when(restTemplate.getForObject(contains("dune1965_djvu.txt"), eq(String.class)))
                .thenReturn("full book text");

        String text = bookService.downloadAndSaveBookText(book);

        assertThat(text).isEqualTo("full book text");
        assertThat(book.getFileContent()).isEqualTo("full book text");
        verify(bookRepository).save(book);
    }

    @Test
    void downloadAndSaveBookTextTruncatesVeryLargeText() {
        Book book = new Book();
        book.setOpenLibraryId("OL1W");
        String huge = "x".repeat(5_000_010);
        when(restTemplate.getForObject(contains("OL1W.json"), eq(String.class)))
                .thenReturn("{\"ocaid\":\"dune1965\"}");
        when(restTemplate.getForObject(contains("_djvu.txt"), eq(String.class))).thenReturn(huge);

        String text = bookService.downloadAndSaveBookText(book);

        assertThat(text).hasSize(5_000_000);
        assertThat(book.getFileContent()).hasSize(5_000_000);
    }

    @Test
    void downloadAndSaveBookTextReturnsNullWhenArchiveDownloadFails() {
        Book book = new Book();
        book.setOpenLibraryId("OL1W");
        when(restTemplate.getForObject(contains("OL1W.json"), eq(String.class)))
                .thenReturn("{\"ocaid\":\"dune1965\"}");
        when(restTemplate.getForObject(contains("_djvu.txt"), eq(String.class)))
                .thenThrow(new RestClientException("boom"));

        assertThat(bookService.downloadAndSaveBookText(book)).isNull();
        verify(bookRepository, never()).save(any());
    }

    @Test
    void fetchInternetArchiveIdReadsIaArrayFromWork() {
        when(restTemplate.getForObject(contains("OL1W.json"), eq(String.class)))
                .thenReturn("{\"ia\":[\"dune1965\"]}");

        assertThat(bookService.fetchInternetArchiveId("OL1W")).isEqualTo("dune1965");
    }

    @Test
    void fetchInternetArchiveIdFallsBackToEditions() {
        when(restTemplate.getForObject(contains("OL1W.json"), eq(String.class))).thenReturn("{}");
        when(restTemplate.getForObject(contains("editions.json"), eq(String.class)))
                .thenReturn("{\"entries\":[{},{\"ia_box_id\":[\"IA123\"]}]}");

        assertThat(bookService.fetchInternetArchiveId("OL1W")).isEqualTo("IA123");
    }

    @Test
    void fetchInternetArchiveIdReturnsNullWhenNothingFound() {
        when(restTemplate.getForObject(contains("OL1W.json"), eq(String.class))).thenReturn("{}");
        when(restTemplate.getForObject(contains("editions.json"), eq(String.class)))
                .thenReturn("{\"entries\":[]}");

        assertThat(bookService.fetchInternetArchiveId("OL1W")).isNull();
    }

    @Test
    void fetchInternetArchiveIdReturnsNullOnError() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RestClientException("boom"));

        assertThat(bookService.fetchInternetArchiveId("OL1W")).isNull();
    }
}
