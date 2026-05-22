package org.example.booknest_2.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class ProfileDTO {
    private String email;
    private LocalDateTime dateReg;
    private List<CollectionDTO> collections;

    public ProfileDTO() {}

}
