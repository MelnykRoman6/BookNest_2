package org.example.booknest_2.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookTest {

    @Test
    void coverUrlUsesOpenLibraryWhenCoverIdPresent() {
        Book book = new Book();
        book.setCoverId("42");

        assertThat(book.getCoverUrl()).isEqualTo("https://covers.openlibrary.org/b/id/42-M.jpg");
    }

    @Test
    void coverUrlFallsBackToPlaceholder() {
        Book nullCover = new Book();
        Book emptyCover = new Book();
        emptyCover.setCoverId("");

        assertThat(nullCover.getCoverUrl()).contains("placeholder");
        assertThat(emptyCover.getCoverUrl()).contains("placeholder");
    }
}
