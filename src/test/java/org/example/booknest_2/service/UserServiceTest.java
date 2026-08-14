package org.example.booknest_2.service;

import org.example.booknest_2.dto.CollectionDTO;
import org.example.booknest_2.dto.ProfileDTO;
import org.example.booknest_2.model.Book;
import org.example.booknest_2.model.Collection;
import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.CollectionRepository;
import org.example.booknest_2.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CollectionRepository collectionRepository;

    @InjectMocks
    private UserService userService;

    private Book book(long id, String title) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        return book;
    }

    private Collection collection(String name, List<Book> books) {
        Collection collection = new Collection();
        collection.setName(name);
        collection.setBooks(books);
        return collection;
    }

    @Test
    void getCollectionsByEmailMapsCollectionsWithBooks() {
        Collection favorites = collection("Favorites", List.of(book(1L, "Dune"), book(2L, "Neuromancer")));
        when(collectionRepository.findByUsersEmail("reader@example.com")).thenReturn(List.of(favorites));

        List<CollectionDTO> dtos = userService.getCollectionsByEmail("reader@example.com");

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getName()).isEqualTo("Favorites");
        assertThat(dtos.get(0).getTotalBooks()).isEqualTo(2);
        assertThat(dtos.get(0).getBookTitles()).containsExactly("Dune", "Neuromancer");
        assertThat(dtos.get(0).getBookIds()).containsExactly("1", "2");
    }

    @Test
    void getCollectionsByEmailHandlesNullBookListAndEmptyResult() {
        Collection empty = collection("Reading", null);
        when(collectionRepository.findByUsersEmail("a@b.c")).thenReturn(List.of(empty));
        when(collectionRepository.findByUsersEmail("none@b.c")).thenReturn(List.of());

        List<CollectionDTO> dtos = userService.getCollectionsByEmail("a@b.c");

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getTotalBooks()).isZero();
        assertThat(dtos.get(0).getBookIds()).isNull();
        assertThat(userService.getCollectionsByEmail("none@b.c")).isEmpty();
    }

    @Test
    void getUserProfileDataMapsEmailRegistrationDateAndCollections() {
        LocalDateTime registered = LocalDateTime.of(2024, 1, 2, 3, 4);
        Users user = new Users();
        user.setEmail("reader@example.com");
        user.setDateReg(registered);
        user.setCollections(Set.of(collection("Favorites", List.of(book(1L, "Dune")))));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        ProfileDTO profile = userService.getUserProfileData(7L);

        assertThat(profile.getEmail()).isEqualTo("reader@example.com");
        assertThat(profile.getDateReg()).isEqualTo(registered);
        assertThat(profile.getCollections()).hasSize(1);
        assertThat(profile.getCollections().get(0).getBookTitles()).containsExactly("Dune");
    }

    @Test
    void getUserProfileDataThrowsWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfileData(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}
