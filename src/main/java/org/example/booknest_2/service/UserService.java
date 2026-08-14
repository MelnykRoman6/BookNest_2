package org.example.booknest_2.service;

import org.example.booknest_2.dto.CollectionDTO;
import org.example.booknest_2.dto.ProfileDTO;
import org.example.booknest_2.model.Collection;
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

    public List<CollectionDTO> getCollectionsByEmail(String email) {
        return collectionRepository.findByUsersEmail(email).stream()
                .map(CollectionDTO::new)
                .collect(Collectors.toList());
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
