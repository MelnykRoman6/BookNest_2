package org.example.booknest_2.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.booknest_2.dto.ExternalBookDTO;
import org.example.booknest_2.model.Author;
import org.example.booknest_2.model.Book;
import org.example.booknest_2.model.SearchHistory;
import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.AuthorRepository;
import org.example.booknest_2.repository.BookRepository;
import org.example.booknest_2.repository.SearchHistoryRepository;
import org.example.booknest_2.util.ExternalIds;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class BookService {

    private static final int MAX_QUERY_LENGTH = 200;
    private static final int MAX_BOOK_TEXT_LENGTH = 5_000_000;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Book> searchLocalBooks(String title, Long authorId, Long genreId, Users currentUser) {
        if (currentUser != null && title != null && !title.isEmpty()) {
            saveSearchHistory(currentUser, title);
        }
        return bookRepository.searchBooks(title, authorId, genreId);
    }

    private void saveSearchHistory(Users user, String query) {

        //verif to not duplicate history searches if they're in the same day
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        var existing = searchHistoryRepository.findByUserAndSearchCriteriaAndSearchDateAfter(user, query, oneDayAgo);

        if (existing.isEmpty()) {
            SearchHistory history = new SearchHistory();
            history.setUser(user);
            history.setSearchCriteria(query);
            history.setSearchDate(LocalDateTime.now());
            searchHistoryRepository.save(history);
        }
    }

    public List<ExternalBookDTO> searchExternalBooks(String query) {
        if (query == null || query.isBlank() || query.length() > MAX_QUERY_LENGTH) {
            return new ArrayList<>();
        }

        String url = UriComponentsBuilder.fromUriString("https://openlibrary.org/search.json")
                .queryParam("q", query)
                .build()
                .encode()
                .toUriString();

        try {
            //api request
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> docs = (List<Map<String, Object>>) response.get("docs");

            List<ExternalBookDTO> results = new ArrayList<>();
            if (docs != null) {
                //first 10 results
                for (int i = 0; i < Math.min(docs.size(), 12); i++) {
                    Map<String, Object> doc = docs.get(i);
                    ExternalBookDTO dto = new ExternalBookDTO();
                    dto.setTitle((String) doc.get("title"));
                    List<String> authors = (List<String>) doc.get("author_name");
                    dto.setAuthorName(authors != null && !authors.isEmpty() ? authors.get(0) : "Unknown");

                    // OpenLibrary ID
                    String key = (String) doc.get("key");
                    dto.setOpenLibraryId(key != null ? key.replace("/works/", "") : "");

                    // cover ID
                    Object coverI = doc.get("cover_i");
                    dto.setCoverId(coverI != null ? coverI.toString() : null);

                    results.add(dto);
                }
            }
            return results;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    public Map<String, Object> getExternalBookDetails(String openLibraryId) {
        if (!ExternalIds.isValidOpenLibraryId(openLibraryId)) {
            return null;
        }
        String url = "https://openlibrary.org/works/" + openLibraryId + ".json";
        try {
            Map<String, Object> details = restTemplate.getForObject(url, Map.class);
            if (details != null) {
                if (details.containsKey("description")) {
                    Object desc = details.get("description");
                    if (desc instanceof Map) {
                        details.put("description", ((Map<?, ?>) desc).get("value"));
                    }
                }

                if (details.containsKey("covers")) {
                    List<Object> covers = (List<Object>) details.get("covers");
                    if (covers != null && !covers.isEmpty()) {
                        details.put("cover_id", covers.get(0).toString());
                    }
                }
            }
            return details;
        } catch (Exception e) {
            return null;
        }
    }

    public Book getOrSaveBookDetails(String id, String authorName) {
        if (!ExternalIds.isValidOpenLibraryId(id)) {
            return null;
        }

        Optional<Book> localBook = bookRepository.findByOpenLibraryId(id);
        if (localBook.isPresent()) {
            return localBook.get();
        }
        return saveExternalBook(id, authorName);
    }

    @Transactional
    public Book saveExternalBook(String openLibraryId, String passedAuthorName) {
        if (!ExternalIds.isValidOpenLibraryId(openLibraryId)) {
            return null;
        }

        //duplicate verif
        Optional<Book> existing = bookRepository.findByOpenLibraryId(openLibraryId);
        if (existing.isPresent()) return existing.get();

        Map<String, Object> details = getExternalBookDetails(openLibraryId);
        if (details == null) return null;

        Book newBook = new Book();
        newBook.setOpenLibraryId(openLibraryId);
        newBook.setTitle((String) details.get("title"));

        if (details.containsKey("description")) {
            Object rawDescription = details.get("description");
            if (rawDescription instanceof String) {
                newBook.setDescription((String) rawDescription);
            } else if (rawDescription instanceof Map) {
                Map<?, ?> descMap = (Map<?, ?>) rawDescription;
                newBook.setDescription((String) descMap.get("value"));
            }
        }

        if (details.containsKey("cover_id")) {
            newBook.setCoverId(details.get("cover_id").toString());
        }

        if (details.containsKey("authors")) {
            List<Map<String, Object>> authorsList = (List<Map<String, Object>>) details.get("authors");
            Set<Author> authorsSet = new HashSet<>();

            for (Map<String, Object> authData : authorsList) {
                Map<String, Object> authRef = (Map<String, Object>) authData.get("author");
                if (authRef != null) {
                    String authKey = ((String) authRef.get("key")).replace("/authors/", "");

                    final String finalAuthName = (passedAuthorName != null) ? passedAuthorName : "Unknown Author";
                    Author author = authorRepository.findByOpenLibraryAuthorId(authKey)
                            .orElseGet(() -> {
                                Author newAuthor = new Author();
                                newAuthor.setOpenLibraryAuthorId(authKey);
                                newAuthor.setName(finalAuthName);
                                return authorRepository.save(newAuthor);
                            });
                    authorsSet.add(author);
                }
            }
            newBook.setAuthors(authorsSet);
        }

        return bookRepository.save(newBook);
    }

    public String downloadAndSaveBookText(Book book) {
        if (book.getFileContent() != null && !book.getFileContent().isEmpty()) {
            return book.getFileContent();
        }

        String olId = book.getOpenLibraryId();
        String iaId = fetchInternetArchiveId(olId);

        if (iaId == null || iaId.isEmpty()) {
            return null;
        }

        try {
            String txtUrl = "https://archive.org/stream/" + iaId + "/" + iaId + "_djvu.txt";
            String fullText = restTemplate.getForObject(txtUrl, String.class);

            if (fullText != null && !fullText.isEmpty()) {
                if (fullText.length() > MAX_BOOK_TEXT_LENGTH) {
                    fullText = fullText.substring(0, MAX_BOOK_TEXT_LENGTH);
                }

                book.setFileContent(fullText);
                bookRepository.save(book);

                return fullText;
            }
        } catch (Exception e) {
            log.warn("Could not download the book text from the Internet Archive", e);
        }

        return null;
    }

    public String fetchInternetArchiveId(String openLibraryId) {
        if (!ExternalIds.isValidOpenLibraryId(openLibraryId)) {
            return null;
        }
        try {
            String workUrl = "https://openlibrary.org/works/" + openLibraryId + ".json";
            String response = restTemplate.getForObject(workUrl, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            String id = extractIaIdFromJson(root);
            if (id != null) return id;

            String editionsUrl = "https://openlibrary.org/works/" + openLibraryId + "/editions.json";
            String editionsResponse = restTemplate.getForObject(editionsUrl, String.class);

            JsonNode editionsRoot = mapper.readTree(editionsResponse);
            if (editionsRoot.has("entries") && editionsRoot.get("entries").isArray()) {
                for (JsonNode edition : editionsRoot.get("entries")) {

                    id = extractIaIdFromJson(edition);
                    if (id != null) {
                        return id;
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Could not resolve the Internet Archive id for an OpenLibrary work", e);
        }
        return null;
    }

    private String extractIaIdFromJson(JsonNode node) {
        String id = null;
        if (node.has("ocaid") && !node.get("ocaid").asText().trim().isEmpty()) {
            id = node.get("ocaid").asText().trim();
        } else if (node.has("ia") && node.get("ia").isArray() && !node.get("ia").isEmpty()) {
            id = node.get("ia").get(0).asText().trim();
        } else if (node.has("ia_box_id") && node.get("ia_box_id").isArray() && !node.get("ia_box_id").isEmpty()) {
            id = node.get("ia_box_id").get(0).asText().trim();
        }
        return ExternalIds.isValidInternetArchiveId(id) ? id : null;
    }
}