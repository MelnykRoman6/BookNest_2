package org.example.booknest_2.service;

import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Users user = usersRepository.findByEmail(email);

        if (user == null) {
            return loadUserByNickname(email);
        }

        return new User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.emptyList()
        );
    }
    
    public UserDetails loadUserByNickname(String nickname) throws UsernameNotFoundException {

        Users user = usersRepository.findByNickname(nickname);

        return new User(
                user.getNickname(),
                user.getPasswordHash(),
                Collections.emptyList()
        );
    }
}