//registration and login controller

package org.example.booknest_2.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.example.booknest_2.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Slf4j
@Controller
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register/confirm")
    public String confirmRegistration(@RequestParam("ver_code") String verificationCode,
                                      @RequestParam String password,
                                      HttpSession session,
                                      Model model) {
        try {
            authService.registerUser(verificationCode, password, session);
        } catch (AuthService.RegistrationException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tempEmail", session.getAttribute(AuthService.SESSION_EMAIL));
            return "register";
        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("error", "Registration failed. Please try again.");
            return "register";
        }
        return "redirect:/login?success";
    }

    @PostMapping("/register/send-code")
    public String sendCode(@RequestParam String email, HttpSession session, Model model) {
        try {
            authService.sendVerificationCode(email, session);
            model.addAttribute("msg", "Code sent to " + email);
            model.addAttribute("tempEmail", email);
        } catch (AuthService.RegistrationException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Sending the verification code failed", e);
            model.addAttribute("error", "Could not send the verification code. Please try again.");
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
