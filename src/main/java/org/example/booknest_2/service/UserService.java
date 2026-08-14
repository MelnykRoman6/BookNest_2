package org.example.booknest_2.service;

import lombok.extern.slf4j.Slf4j;
import org.example.booknest_2.dto.CollectionDTO;
import org.example.booknest_2.dto.ProfileDTO;
import org.example.booknest_2.model.Collection;
import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.*;
import org.example.booknest_2.util.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;

import java.util.List;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private CollectionRepository collectionRepository;

    public Users getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(NotFoundException.of("User", email));
    }

    public List<CollectionDTO> getCollectionsByEmail(String email) {
        List<Collection> collections = collectionRepository.findByUsersEmail(email);
        log.debug("found {} collections for user {}", collections.size(), email);

        return CollectionDTO.fromEntities(collections);
    }

    public ProfileDTO getUserProfileData(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(NotFoundException.of("User", userId));

        ProfileDTO dto = new ProfileDTO();
        dto.setEmail(user.getEmail());
        dto.setDateReg(user.getDateReg());
        dto.setCollections(CollectionDTO.fromEntities(user.getCollections()));

        return dto;
    }
}
