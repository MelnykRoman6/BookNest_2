//registration and login controller

package org.example.booknest_2.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.example.booknest_2.exception.RegistrationSessionException;
import org.example.booknest_2.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register/confirm")
    public String confirmRegistration(@RequestParam String password,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        try {
            authService.registerUser(password, session);
        } catch (RegistrationSessionException e) {
            log.warn("Registration confirmed without a verified email in session", e);
            redirectAttributes.addFlashAttribute("error", "Your registration session expired, please start again.");
            return "redirect:/register";
        } catch (DataIntegrityViolationException e) {
            log.warn("Registration rejected by the database", e);
            redirectAttributes.addFlashAttribute("error", "This email is already registered.");
            return "redirect:/register";
        }
        return "redirect:/login?success";
    }

    @PostMapping("/register/send-code")
    public String sendCode(@RequestParam String email, HttpSession session, Model model) {
        try {
            authService.sendVerificationCode(email, session);
            model.addAttribute("msg", "Code sent to " + email);
            model.addAttribute("tempEmail", email);
        } catch (MailException e) {
            log.error("Could not send verification code to {}", email, e);
            model.addAttribute("error", "We could not send the code right now, please try again later.");
        }
        return "register";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
