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
    public String addBookToCollection(@RequestParam String bookId,
                                      @RequestParam Long collectionId,
                                      @RequestParam String olId,
                                      RedirectAttributes redirectAttributes) {
        System.out.println("--- ADD BOOK TO COLLECTION ---");
        System.out.println("Received bookId: " + bookId);
        System.out.println("Received collectionId: " + collectionId);
        System.out.println("Received olId from form: " + olId);
        try {
            collectionService.addBookToCollection(bookId, collectionId);
            redirectAttributes.addFlashAttribute("success", "Book successfully added!");
        } catch (Exception e) {
            System.out.println("Error inside controller: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error while adding: " + e.getMessage());
        }

        return "redirect:/book/" + olId;
    }
}