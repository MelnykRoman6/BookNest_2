package org.example.booknest_2.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.booknest_2.util.OpenLibraryUrls;

@Getter
@Setter
public class ExternalBookDTO {
    private String openLibraryId;
    private String title;
    private String authorName;
    private String coverId;
    private String iaId;

    public String getCoverUrl() {
        return OpenLibraryUrls.cover(coverId);
    }
}