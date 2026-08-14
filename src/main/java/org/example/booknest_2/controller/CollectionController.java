//controller for all operations with collections

package org.example.booknest_2.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.booknest_2.exception.ResourceNotFoundException;
import org.example.booknest_2.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
                                      RedirectAttributes redirectAttributes) {
        log.debug("Adding book {} to collection {} (olId {})", bookId, collectionId, olId);
        try {
            collectionService.addBookToCollection(bookId, collectionId);
            redirectAttributes.addFlashAttribute("success", "Book successfully added!");
        } catch (ResourceNotFoundException e) {
            log.warn("Could not add book {} to collection {}", bookId, collectionId, e);
            redirectAttributes.addFlashAttribute("error", "This book or collection no longer exists.");
        } catch (DataAccessException e) {
            log.error("Failed to add book {} to collection {}", bookId, collectionId, e);
            redirectAttributes.addFlashAttribute("error", "Could not add the book right now, please try again.");
        }

        return "redirect:/book/" + olId;
    }
}