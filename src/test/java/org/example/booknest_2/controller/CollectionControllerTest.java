package org.example.booknest_2.controller;

import org.example.booknest_2.service.CollectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CollectionControllerTest {

    @Mock
    private CollectionService collectionService;

    @InjectMocks
    private CollectionController collectionController;

    @Test
    void addBookToCollectionRedirectsWithSuccessFlash() {
        RedirectAttributes attributes = new RedirectAttributesModelMap();

        String view = collectionController.addBookToCollection("1", 5L, "OL1W", attributes);

        assertThat(view).isEqualTo("redirect:/book/OL1W");
        assertThat(attributes.getFlashAttributes().get("success")).isEqualTo("Book successfully added!");
        verify(collectionService).addBookToCollection("1", 5L);
    }

    @Test
    void addBookToCollectionRedirectsWithErrorFlashOnFailure() {
        doThrow(new RuntimeException("Collection not found"))
                .when(collectionService).addBookToCollection("1", 5L);
        RedirectAttributes attributes = new RedirectAttributesModelMap();

        String view = collectionController.addBookToCollection("1", 5L, "OL1W", attributes);

        assertThat(view).isEqualTo("redirect:/book/OL1W");
        assertThat(attributes.getFlashAttributes().get("error"))
                .isEqualTo("Error while adding: Collection not found");
    }
}
