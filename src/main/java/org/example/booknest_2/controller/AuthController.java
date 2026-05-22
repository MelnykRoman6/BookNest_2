//registration and login controller

package org.example.booknest_2.controller;

import jakarta.servlet.http.HttpSession;
import org.example.booknest_2.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register/confirm")
    public String confirmRegistration(@RequestParam String password, HttpSession session) {
        authService.registerUser(password, session);
        return "redirect:/login?success";
    }

    @PostMapping("/register/send-code")
    public String sendCode(@RequestParam String email, HttpSession session, Model model) {
        System.out.println("authContr debug: received mail: " + email);
        try {
            authService.sendVerificationCode(email, session);
            model.addAttribute("msg", "Code sent to " + email);
            model.addAttribute("tempEmail", email);
            System.out.println("try");
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
