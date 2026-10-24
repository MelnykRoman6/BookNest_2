package org.example.booknest_2.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import org.example.booknest_2.model.SearchHistory;

@Setter
@Getter
public class SearchHistoryDTO {
    private String searchCriteria;
    private LocalDateTime searchDate;

    public SearchHistoryDTO(SearchHistory searchHistory) {
        this.searchCriteria = searchHistory.getSearchCriteria();
        this.searchDate = searchHistory.getSearchDate();
    }
}