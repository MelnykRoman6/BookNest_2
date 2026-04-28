package org.example.booknest_2.service;


import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.CollectionRepository;
import org.example.booknest_2.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.example.booknest_2.model.Collection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class AuthService {
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private CollectionRepository collectionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationCode(String email, HttpSession session) {
        String code = String.valueOf(new Random().nextInt(899999) + 100000);
        session.setAttribute("temp_code", code);
        session.setAttribute("temp_email", email);
        System.out.println("send verif code in");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("melnykromandev@gmail.com");
        message.setTo(email);
        message.setSubject("BookNest Code");
        message.setText("Your code: " + code);

        mailSender.send(message);
        System.out.println("verif sent end");
    }

    @Transactional
    public void registerUser(String password, HttpSession session) {
        System.out.println("register user in");
        Users newUser = new Users();
        String email = (String) session.getAttribute("temp_email");
        System.out.println("got email: " + email);
        if (email != null) {
            newUser.setEmail(email);
        }
        newUser.setVerified(true);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setDateReg(LocalDateTime.now());
        newUser = userRepository.save(newUser);

        List<String> baseCollections = List.of("Reading", "Finished", "I want to read", "Favorites");
        for (String name : baseCollections) {
            Collection col = new Collection();
            col.setName(name);
            col.setUsers(newUser);
            collectionRepository.save(col);
        }
    }
}