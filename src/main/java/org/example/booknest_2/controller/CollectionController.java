//controller for all operations with collections

package org.example.booknest_2.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.booknest_2.service.CollectionService;
import org.example.booknest_2.util.ExternalIds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @PostMapping("/collection/add-book")
    public String addBookToCollection(@RequestParam String bookId,
                                      @RequestParam Long collectionId,
                                      @RequestParam String olId,
                                      @AuthenticationPrincipal UserDetails currentUser,
                                      RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!ExternalIds.isValidOpenLibraryId(olId)) {
            redirectAttributes.addFlashAttribute("error", "Unknown book.");
            return "redirect:/";
        }

        try {
            collectionService.addBookToCollection(bookId, collectionId, currentUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Book successfully added!");
        } catch (Exception e) {
            log.warn("Could not add a book to a collection", e);
            redirectAttributes.addFlashAttribute("error", "The book could not be added.");
        }

        return "redirect:/book/" + olId;
    }
}
