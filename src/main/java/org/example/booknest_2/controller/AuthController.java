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
    public String confirmRegistration(@RequestParam String password, @RequestParam String nickname, HttpSession session) {
        authService.registerUser(password, nickname, session);
        return "redirect:/login?success";
    }

    @PostMapping("/register/send-code")
    public String sendCode(@RequestParam String email, HttpSession session, Model model) {
        log.info("authContr debug: received mail: " + email);
        try {
            authService.sendVerificationCode(email, session);
            model.addAttribute("msg", "Code sent to " + email);
            model.addAttribute("tempEmail", email);
            log.info("try");
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
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
