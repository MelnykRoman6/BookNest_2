package org.example.booknest_2.service;

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
import org.example.booknest_2.util.InternetArchiveUrls;
import org.example.booknest_2.util.OpenLibraryParser;
import org.example.booknest_2.util.OpenLibraryUrls;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookService {
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
        //forming openlibraryid
        String url = OpenLibraryUrls.search(query);

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
                    dto.setOpenLibraryId(OpenLibraryUrls.workIdFromKey((String) doc.get("key")));

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
        String url = OpenLibraryUrls.work(openLibraryId);
        try {
            Map<String, Object> details = restTemplate.getForObject(url, Map.class);
            if (details != null) {
                if (details.containsKey("description")) {
                    details.put("description", OpenLibraryParser.extractDescription(details.get("description")));
                }

                String coverId = OpenLibraryParser.extractFirstCoverId(details.get("covers"));
                if (coverId != null) {
                    details.put("cover_id", coverId);
                }
            }
            return details;
        } catch (Exception e) {
            return null;
        }
    }

    public Book getOrSaveBookDetails(String id, String authorName) {
        Optional<Book> localBook = bookRepository.findByOpenLibraryId(id);
        System.out.println("bookService DEBUG: Service searching ID: " + id);
        System.out.println("bookService DEBUG: Service received Author Name: " + authorName);

        if (localBook.isEmpty()) {
            try {
                localBook = bookRepository.findByOpenLibraryId(id);
            } catch (NumberFormatException ignored) {}
        }

        if (localBook.isPresent()) {
            return localBook.get();
        } else {
            System.out.println("bookService debug: Book not found locally. Saving external book from OL...");
            return saveExternalBook(id, authorName);
        }
    }

    @Transactional
    public Book saveExternalBook(String openLibraryId, String passedAuthorName) {
        //duplicate verif
        Optional<Book> existing = bookRepository.findByOpenLibraryId(openLibraryId);
        if (existing.isPresent()) return existing.get();

        Map<String, Object> details = getExternalBookDetails(openLibraryId);
        if (details == null) return null;

        Book newBook = new Book();
        newBook.setOpenLibraryId(openLibraryId);
        newBook.setTitle((String) details.get("title"));

        if (details.containsKey("description")) {
            newBook.setDescription(OpenLibraryParser.extractDescription(details.get("description")));
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
                    String authKey = OpenLibraryUrls.authorIdFromKey((String) authRef.get("key"));

                    final String finalAuthName = (passedAuthorName != null) ? passedAuthorName : "Unknown Author";
                    System.out.println("DEBUG: Saving book with author name: " + passedAuthorName);
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
        System.out.println("START AUTOMATIC DOWNLOAD FOR BOOK: " + olId);

        String iaId = fetchInternetArchiveId(olId);

        if (iaId == null || iaId.isEmpty()) {
            System.out.println("API OpenLibrary не предоставило IA ID для " + olId);
            return null;
        }

        try {
            String txtUrl = InternetArchiveUrls.fullText(iaId);

            System.out.println("Downloading full text from: " + txtUrl);
            String fullText = restTemplate.getForObject(txtUrl, String.class);

            if (fullText != null && !fullText.isEmpty()) {
                if (fullText.length() > 5000000) {
                    fullText = fullText.substring(0, 5000000);
                }

                book.setFileContent(fullText);
                bookRepository.save(book);

                System.out.println("SUCCESS: Text downloaded and saved to DB. Size: " + fullText.length() + " chars.");
                return fullText;
            }
        } catch (Exception e) {
            System.out.println("err downloading txt from Internet Archive: " + e.getMessage());
        }

        return null;
    }

    public String fetchInternetArchiveId(String openLibraryId) {
        try {
            String workUrl = OpenLibraryUrls.work(openLibraryId);
            String response = restTemplate.getForObject(workUrl, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            String id = OpenLibraryParser.extractIaId(root);
            if (id != null) return id;

            System.out.println("IA ID not found: " + openLibraryId);
            String editionsUrl = OpenLibraryUrls.editions(openLibraryId);
            String editionsResponse = restTemplate.getForObject(editionsUrl, String.class);

            JsonNode editionsRoot = mapper.readTree(editionsResponse);
            if (editionsRoot.has("entries") && editionsRoot.get("entries").isArray()) {
                for (JsonNode edition : editionsRoot.get("entries")) {

                    id = OpenLibraryParser.extractIaId(edition);
                    if (id != null) {
                        System.out.println("iaid: " + id);
                        return id;
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("err " + openLibraryId + ": " + e.getMessage());
        }
        return null;
    }
}