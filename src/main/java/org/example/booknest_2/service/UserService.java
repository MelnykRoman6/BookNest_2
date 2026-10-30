package org.example.booknest_2.service;

import org.example.booknest_2.dto.CollectionDTO;
import org.example.booknest_2.dto.ProfileDTO;
import org.example.booknest_2.dto.SearchHistoryDTO;
import org.example.booknest_2.model.Collection;
import org.example.booknest_2.model.SearchHistory;
import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CollectionRepository collectionRepository;
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    public List<CollectionDTO> getCollectionsByEmailorNickname(String username) {
        System.out.println("userService debug: searching collections for user: " + username);
        List<Collection> collections = collectionRepository.findByUsersEmail(username);
        System.out.println("userService debug: collections found: " + collections.size());
        for (Collection col : collections) {
            System.out.println("userService debug: collection: " + col.getName());
            if (col.getBooks() == null) {
                System.out.println("userService debug: books array is null");
            } else {
                System.out.println("userService debug: total qta of books: " + col.getBooks().size());
                col.getBooks().forEach(b ->
                        System.out.println("userService debug: books in collection: ID=" + b.getOpenLibraryId() + ", Title=" + b.getTitle())
                );
            }
        }

        System.out.println("userService debug: qta of entities of books: " + collections.size());
        
        for (Collection c : collections) {
            System.out.println("userService debug: entity from db - ID: " + c.getId() + ", Name: " + c.getName());
        }

        List<CollectionDTO> dtos = collections.stream()
                .map(col -> new CollectionDTO(col))
                .collect(Collectors.toList());

        System.out.println("userService debug: qta after mapping: " + dtos.size());

        return dtos;
    }

    public List<SearchHistoryDTO> getSearchHistoryByEmail(String email) {
        List<SearchHistory> searchHistory = searchHistoryRepository.findByUsersEmail(email);
        System.out.println("userService debug: qta of entities of search history: " + searchHistory.size());
        List<SearchHistoryDTO> dtos = searchHistory.stream()
                .map(sh -> new SearchHistoryDTO(sh))
                .collect(Collectors.toList());

        return dtos;
    }

    public ProfileDTO getUserProfileData(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProfileDTO dto = new ProfileDTO();
        dto.setEmail(user.getEmail());
        dto.setDateReg(user.getDateReg());

        dto.setCollections(user.getCollections().stream()
                .map(c -> new CollectionDTO(c))
                .toList());

        return dto;
    }
}
