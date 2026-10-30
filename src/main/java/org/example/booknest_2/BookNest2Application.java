package org.example.booknest_2;

import org.example.booknest_2.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BookNest2Application {

    public static void main(String[] args) {
        SpringApplication.run(BookNest2Application.class, args);
    }

    @Bean
    CommandLineRunner testDb(BookRepository bookRepository) {
        return args -> {
            System.out.println( "BookNest2Application initial testDb");
            System.out.println("--- CHECKING DATABASE ---");
            long count = bookRepository.count();
            System.out.println("Total books in database: " + count);
            System.out.println("-------------------------");
        };
    }
}
