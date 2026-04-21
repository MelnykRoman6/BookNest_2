package org.example.booknest_2.controller;

import org.example.booknest_2.dto.ProfileDTO;
import org.example.booknest_2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfile(Model model) {

        Long currentUserId = 1L;

        ProfileDTO profile = userService.getUserProfileData(currentUserId);
        model.addAttribute("user", profile);

        return "profile";
    }
}
