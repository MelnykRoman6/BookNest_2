package org.example.booknest_2.controller;

import org.example.booknest_2.dto.CollectionDTO;
import org.example.booknest_2.model.Collection;
import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.UsersRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private UserController userController;

    private UserDetails principal(String email) {
        return new User(email, "hashed", List.of());
    }

    @Test
    void showProfileRedirectsAnonymousUserToLogin() {
        assertThat(userController.showProfile(new ConcurrentModel(), null)).isEqualTo("redirect:/login");
    }

    @Test
    void showProfileExposesUserAndCollections() {
        Users user = new Users();
        user.setEmail("reader@example.com");
        Collection collection = new Collection();
        collection.setName("Favorites");
        CollectionDTO dto = new CollectionDTO(collection);
        when(usersRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(userService.getCollectionsByEmail("reader@example.com")).thenReturn(List.of(dto));
        Model model = new ConcurrentModel();

        String view = userController.showProfile(model, principal("reader@example.com"));

        assertThat(view).isEqualTo("profile");
        assertThat(model.getAttribute("user")).isSameAs(user);
        assertThat(model.getAttribute("collections")).isEqualTo(List.of(dto));
    }

    @Test
    void showProfileThrowsWhenAuthenticatedUserIsUnknown() {
        when(usersRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.showProfile(new ConcurrentModel(), principal("ghost@example.com")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}
