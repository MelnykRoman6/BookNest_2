package org.example.booknest_2.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalBookDTOTest {

    @Test
    void coverUrlUsesOpenLibraryWhenCoverIdPresent() {
        ExternalBookDTO dto = new ExternalBookDTO();
        dto.setCoverId("42");

        assertThat(dto.getCoverUrl()).isEqualTo("https://covers.openlibrary.org/b/id/42-M.jpg");
    }

    @Test
    void coverUrlFallsBackToPlaceholder() {
        ExternalBookDTO nullCover = new ExternalBookDTO();
        ExternalBookDTO emptyCover = new ExternalBookDTO();
        emptyCover.setCoverId("");

        assertThat(nullCover.getCoverUrl()).contains("placeholder");
        assertThat(emptyCover.getCoverUrl()).contains("placeholder");
    }
}
