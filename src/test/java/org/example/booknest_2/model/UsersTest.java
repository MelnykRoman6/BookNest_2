package org.example.booknest_2.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UsersTest {

    @Test
    void dateRegAliasesRegistrationDate() {
        LocalDateTime registered = LocalDateTime.of(2024, 5, 6, 7, 8);
        Users user = new Users();
        user.setDateReg(registered);

        assertThat(user.getDateReg()).isEqualTo(registered);
        assertThat(user.getRegistrationDate()).isEqualTo(registered);
    }
}
