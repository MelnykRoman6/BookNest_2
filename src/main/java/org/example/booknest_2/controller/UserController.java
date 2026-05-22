//controller for all operations with users

package org.example.booknest_2.controller;

import org.example.booknest_2.dto.CollectionDTO;
import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.UsersRepository;
import org.example.booknest_2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/profile")
    public String showProfile(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        String email = currentUser.getUsername();

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CollectionDTO> userCollections = userService.getCollectionsByEmail(email);

        model.addAttribute("user", user);
        model.addAttribute("collections", userCollections);

        return "profile";
    }
}
