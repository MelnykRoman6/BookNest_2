package org.example.booknest_2.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class ProfileDTO {
    private String email;
    private LocalDateTime dateReg;
    private List<CollectionDTO> collections;

    public ProfileDTO() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getDateReg() { return dateReg; }
    public void setDateReg(LocalDateTime dateReg) { this.dateReg = dateReg; }

    public List<CollectionDTO> getCollections() { return collections; }
    public void setCollections(List<CollectionDTO> collections) { this.collections = collections; }
}
