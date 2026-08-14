package org.example.booknest_2.service;

import org.example.booknest_2.model.Collection;
import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.CollectionRepository;
import org.example.booknest_2.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsersRepository userRepository;
    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private AuthService authService;

    @Test
    void sendVerificationCodeStoresSixDigitCodeInSessionAndMailsIt() {
        MockHttpSession session = new MockHttpSession();

        authService.sendVerificationCode("reader@example.com", session);

        String code = (String) session.getAttribute("temp_code");
        assertThat(code).hasSize(6).containsOnlyDigits();
        assertThat(session.getAttribute("temp_email")).isEqualTo("reader@example.com");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getTo()).containsExactly("reader@example.com");
        assertThat(message.getSubject()).isEqualTo("BookNest Code");
        assertThat(message.getText()).isEqualTo("Your code: " + code);
    }

    @Test
    void sendVerificationCodePropagatesMailFailures() {
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> authService.sendVerificationCode("reader@example.com", new MockHttpSession()))
                .isInstanceOf(MailSendException.class);
    }

    @Test
    void registerUserCreatesVerifiedUserWithBaseCollections() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("temp_email", "reader@example.com");
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.registerUser("secret", session);

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(userCaptor.capture());
        Users saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("reader@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.isVerified()).isTrue();
        assertThat(saved.getDateReg()).isNotNull();

        ArgumentCaptor<Collection> collectionCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(collectionRepository, times(4)).save(collectionCaptor.capture());
        assertThat(collectionCaptor.getAllValues())
                .extracting(Collection::getName)
                .containsExactly("Reading", "Finished", "I want to read", "Favorites");
        assertThat(collectionCaptor.getAllValues())
                .allSatisfy(collection -> assertThat(collection.getUsers()).isSameAs(saved));
    }

    @Test
    void registerUserLeavesEmailUnsetWhenSessionHasNoEmail() {
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.registerUser("secret", new MockHttpSession());

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isNull();
    }
}
