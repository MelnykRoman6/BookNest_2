package org.example.booknest_2.controller;

import org.example.booknest_2.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void confirmRegistrationDelegatesAndRedirectsToLogin() {
        MockHttpSession session = new MockHttpSession();

        String view = authController.confirmRegistration("secret", session);

        assertThat(view).isEqualTo("redirect:/login?success");
        verify(authService).registerUser("secret", session);
    }

    @Test
    void sendCodeExposesConfirmationMessage() {
        MockHttpSession session = new MockHttpSession();
        Model model = new ConcurrentModel();

        String view = authController.sendCode("reader@example.com", session, model);

        assertThat(view).isEqualTo("register");
        assertThat(model.getAttribute("msg")).isEqualTo("Code sent to reader@example.com");
        assertThat(model.getAttribute("tempEmail")).isEqualTo("reader@example.com");
        verify(authService).sendVerificationCode("reader@example.com", session);
    }

    @Test
    void sendCodeExposesErrorWhenMailFails() {
        MockHttpSession session = new MockHttpSession();
        doThrow(new MailSendException("smtp down"))
                .when(authService).sendVerificationCode("reader@example.com", session);
        Model model = new ConcurrentModel();

        String view = authController.sendCode("reader@example.com", session, model);

        assertThat(view).isEqualTo("register");
        assertThat(model.getAttribute("error")).isEqualTo("Error: smtp down");
        assertThat(model.asMap()).doesNotContainKey("msg");
    }

    @Test
    void staticPagesResolveToTemplates() {
        assertThat(authController.registerPage()).isEqualTo("register");
        assertThat(authController.loginPage()).isEqualTo("login");
    }
}
