package org.example.booknest_2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalBookDTO {
    private String openLibraryId;
    private String title;
    private String authorName;
    private String coverId;
    private String iaId;

    public String getCoverUrl() {
        return (coverId != null && !coverId.isEmpty())
                ? "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg"
                : "https://via.placeholder.com/100x150?text=No+Cover";
    }
}