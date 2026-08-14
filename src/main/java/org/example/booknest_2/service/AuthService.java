package org.example.booknest_2.service;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.example.booknest_2.model.Users;
import org.example.booknest_2.repository.CollectionRepository;
import org.example.booknest_2.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.example.booknest_2.model.Collection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AuthService {

    public static final String SESSION_CODE = "temp_code";
    public static final String SESSION_EMAIL = "temp_email";
    public static final String SESSION_CODE_ISSUED_AT = "temp_code_issued_at";

    private static final Duration CODE_TTL = Duration.ofMinutes(15);
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private CollectionRepository collectionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${booknest.mail.from}")
    private String mailFrom;

    /**
     * Thrown when registration input fails validation. The message is safe to show to the user.
     */
    public static class RegistrationException extends RuntimeException {
        public RegistrationException(String message) {
            super(message);
        }
    }

    //sends the verif code to the user
    public void sendVerificationCode(String email, HttpSession session) {
        String normalizedEmail = normalizeEmail(email);
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new RegistrationException("Please enter a valid email address.");
        }

        String code = String.valueOf(secureRandom.nextInt(900000) + 100000);
        session.setAttribute(SESSION_CODE, code);
        session.setAttribute(SESSION_EMAIL, normalizedEmail);
        session.setAttribute(SESSION_CODE_ISSUED_AT, LocalDateTime.now());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(normalizedEmail);
        message.setSubject("BookNest Code");
        message.setText("Your code: " + code);

        mailSender.send(message);
    }

    @Transactional
    public void registerUser(String verificationCode, String password, HttpSession session) {
        String email = (String) session.getAttribute(SESSION_EMAIL);
        String expectedCode = (String) session.getAttribute(SESSION_CODE);
        LocalDateTime issuedAt = (LocalDateTime) session.getAttribute(SESSION_CODE_ISSUED_AT);

        if (email == null || expectedCode == null || issuedAt == null) {
            throw new RegistrationException("Request a verification code first.");
        }
        if (issuedAt.plus(CODE_TTL).isBefore(LocalDateTime.now())) {
            clearVerificationState(session);
            throw new RegistrationException("The verification code has expired. Request a new one.");
        }
        if (verificationCode == null || !constantTimeEquals(expectedCode, verificationCode.trim())) {
            throw new RegistrationException("The verification code is incorrect.");
        }
        if (password == null
                || password.length() < MIN_PASSWORD_LENGTH
                || password.length() > MAX_PASSWORD_LENGTH) {
            throw new RegistrationException(
                    "The password must be between " + MIN_PASSWORD_LENGTH
                            + " and " + MAX_PASSWORD_LENGTH + " characters.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            clearVerificationState(session);
            throw new RegistrationException("An account with this email already exists.");
        }

        //the code is single use
        clearVerificationState(session);

        Users newUser = new Users();
        newUser.setEmail(email);
        newUser.setVerified(true);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setDateReg(LocalDateTime.now());
        newUser = userRepository.save(newUser);

        //right after user creation creates also basic collections
        List<String> baseCollections = List.of("Reading", "Finished", "I want to read", "Favorites");
        for (String name : baseCollections) {
            Collection col = new Collection();
            col.setName(name);
            col.setUsers(newUser);
            collectionRepository.save(col);
        }
    }

    private void clearVerificationState(HttpSession session) {
        session.removeAttribute(SESSION_CODE);
        session.removeAttribute(SESSION_CODE_ISSUED_AT);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
