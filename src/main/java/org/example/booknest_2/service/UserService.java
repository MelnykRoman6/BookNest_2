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
        System.out.println("--- ДЕБАГ СЕРВИСА ---");
        System.out.println("Ищем коллекции для email: " + email);
        List<Collection> collections = collectionRepository.findByUsersEmail(email);
        for (Collection col : collections) {
            System.out.println("Коллекция: " + col.getName());
            if (col.getBooks() == null) {
                System.out.println(" -> Список книг NULL!");
            } else {
                System.out.println(" -> Размер списка книг в Entity: " + col.getBooks().size());
                col.getBooks().forEach(b ->
                        System.out.println("   -> Книга в Entity: ID=" + b.getId() + ", Title=" + b.getTitle())
                );
            }
        }
        System.out.println("=============================");
        System.out.println("Количество сырых сущностей из базы: " + collections.size());
        for (Collection c : collections) {
            System.out.println("Сущность из БД -> ID: " + c.getId() + ", Name: " + c.getName());
        }

        List<CollectionDTO> dtos = collections.stream()
                .map(col -> new CollectionDTO((long) col.getId(), col.getName()))
                .collect(Collectors.toList());

        System.out.println("Количество DTO после маппинга: " + dtos.size());
        System.out.println("---------------------");

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
