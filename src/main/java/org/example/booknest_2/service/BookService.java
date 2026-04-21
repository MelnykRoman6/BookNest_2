package org.example.booknest_2.service;

import org.example.booknest_2.dto.ExternalBookDTO;
import org.example.booknest_2.model.Author;
import org.example.booknest_2.model.Book;
import org.example.booknest_2.model.SearchHistory;
import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.AuthorRepository;
import org.example.booknest_2.repository.BookRepository;
import org.example.booknest_2.repository.SearchHistoryRepository;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
        // Проверяем, не искал ли юзер это же самое в последние 24 часа
        // (Аналог твоей проверки CURDATE() в PHP)
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
        // Формируем URL для OpenLibrary (как в твоем PHP)
        String url = "https://openlibrary.org/search.json?q=" + query.replace(" ", "+");

        try {
            // Делаем запрос. Map.class говорит Spring, что мы ждем JSON, который нужно превратить в карту
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> docs = (List<Map<String, Object>>) response.get("docs");

            List<ExternalBookDTO> results = new ArrayList<>();
            if (docs != null) {
                // Берем первые 10 результатов, чтобы не перегружать страницу
                for (int i = 0; i < Math.min(docs.size(), 12); i++) {
                    Map<String, Object> doc = docs.get(i);
                    ExternalBookDTO dto = new ExternalBookDTO();

                    // Вытаскиваем данные из JSON (аналог $book['title'] и т.д.)
                    dto.setTitle((String) doc.get("title"));

                    List<String> authors = (List<String>) doc.get("author_name");
                    dto.setAuthorName(authors != null && !authors.isEmpty() ? authors.get(0) : "Unknown");

                    // OpenLibrary ID (нужен для ссылки на страницу книги)
                    String key = (String) doc.get("key");
                    dto.setOpenLibraryId(key != null ? key.replace("/works/", "") : "");

                    // ID обложки
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
        String url = "https://openlibrary.org/works/" + openLibraryId + ".json";
        try {
            Map<String, Object> details = restTemplate.getForObject(url, Map.class);
            if (details != null) {
                // Обработка описания (уже была)
                if (details.containsKey("description")) {
                    Object desc = details.get("description");
                    if (desc instanceof Map) {
                        details.put("description", ((Map<?, ?>) desc).get("value"));
                    }
                }

                // НОВОЕ: Обработка обложки
                if (details.containsKey("covers")) {
                    List<Object> covers = (List<Object>) details.get("covers");
                    if (covers != null && !covers.isEmpty()) {
                        // Берем первый ID из массива и сохраняем как cover_id
                        details.put("cover_id", covers.get(0).toString());
                    }
                }
            }
            return details;
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public Book saveExternalBook(String openLibraryId, String passedAuthorName) {
        // 1. Проверка на дубликат (чтобы не сохранять одну и ту же книгу дважды)
        Optional<Book> existing = bookRepository.findByOpenLibraryId(openLibraryId);
        if (existing.isPresent()) return existing.get();

        // 2. Получаем полные данные из API
        Map<String, Object> details = getExternalBookDetails(openLibraryId);
        if (details == null) return null;

        // 3. Создаем объект Книги и заполняем поля
        Book newBook = new Book();
        newBook.setOpenLibraryId(openLibraryId);
        newBook.setTitle((String) details.get("title"));

        if (details.containsKey("description")) {
            Object rawDescription = details.get("description");
            if (rawDescription instanceof String) {
                newBook.setDescription((String) rawDescription);
            } else if (rawDescription instanceof Map) {
                // Если описание пришло как {"value": "текст..."}, достаем value
                Map<?, ?> descMap = (Map<?, ?>) rawDescription;
                newBook.setDescription((String) descMap.get("value"));
            }
        }

        // Сохраняем ID обложки
        if (details.containsKey("cover_id")) {
            newBook.setCoverId(details.get("cover_id").toString());
        }

        // 4. ЛОГИКА АВТОРОВ (Связи Many-to-Many)
        if (details.containsKey("authors")) {
            List<Map<String, Object>> authorsList = (List<Map<String, Object>>) details.get("authors");
            Set<Author> authorsSet = new HashSet<>();

            // В цикле перебора авторов:
            for (Map<String, Object> authData : authorsList) {
                Map<String, Object> authRef = (Map<String, Object>) authData.get("author");
                if (authRef != null) {
                    String authKey = ((String) authRef.get("key")).replace("/authors/", "");

                    String authName = (String) authData.get("name");
                    if (authName == null) authName = "Unknown Author";

                    String rawName = (String) authData.get("name");
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
}