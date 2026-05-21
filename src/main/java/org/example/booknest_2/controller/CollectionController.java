package org.example.booknest_2.controller;

import org.example.booknest_2.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @PostMapping("/collection/add-book")
    public String addBookToCollection(@RequestParam("bookId") String bookId,
                                      @RequestParam("collectionId") Long collectionId,
                                      RedirectAttributes redirectAttributes) {
        try {
            collectionService.addBookToCollection(bookId, collectionId);
            redirectAttributes.addFlashAttribute("success", "Book added to collection!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/book/" + bookId;
    }
}