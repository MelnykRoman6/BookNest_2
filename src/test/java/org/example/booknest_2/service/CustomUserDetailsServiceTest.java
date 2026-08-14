package org.example.booknest_2.service;

import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameMapsEmailAndPasswordHash() {
        Users user = new Users();
        user.setEmail("reader@example.com");
        user.setPasswordHash("hashed");
        when(usersRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("reader@example.com");

        assertThat(details.getUsername()).isEqualTo("reader@example.com");
        assertThat(details.getPassword()).isEqualTo("hashed");
        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    void loadUserByUsernameThrowsForUnknownEmail() {
        when(usersRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("ghost@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost@example.com");
    }
}
