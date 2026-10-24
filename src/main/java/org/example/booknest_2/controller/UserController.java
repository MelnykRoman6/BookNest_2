//controller for all operations with users

package org.example.booknest_2.controller;

import org.example.booknest_2.dto.CollectionDTO;
import org.example.booknest_2.dto.SearchHistoryDTO;
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

        String username = currentUser.getUsername();
        Users user = null;
        String userEmail = null;

        if (username.contains("@")) {
            System.out.println("userController debug: user is logged in with email: " + username);
            user = usersRepository.findByEmail(username);
        } else {
            System.out.println("userController debug: user is logged in with nickname: " + username);
            user = usersRepository.findByNickname(username);
        }

        userEmail = user.getEmail();
        
        System.out.println("USER ID: " + user.getId());
        System.out.println("USERNAME: " + user.getEmail());
        System.out.println("NICKNAME: " + user.getNickname());

        List<CollectionDTO> userCollections = userService.getCollectionsByEmailorNickname(userEmail);

        List<SearchHistoryDTO> userSearchHistory = userService.getSearchHistoryByEmail(userEmail);

        model.addAttribute("user", user);
        model.addAttribute("collections", userCollections);
        model.addAttribute("searchHistory", userSearchHistory);

        return "profile";
    }
}
