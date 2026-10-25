# 📚 BookNest 2
A web application for cataloging books, organizing personal reading shelves, and viewing works online with integration of open library catalogs.

The project was developed on the Java / Spring Boot stack with server-side page rendering via Thymeleaf, secure authorization via Spring Security, and containerized PostgreSQL storage.

## 🚀 Main Functionality
### Authentication and Security:
User registration and login with data validation.
Secure password hashing using the BCryptPasswordEncoder algorithm.
Account confirmation via verification links sent by email.
Access level control for pages and resources via SecurityConfig.

### Search and Catalog:
Search for works by titles, authors, and categories.
Integration with the external OpenLibrary API service to dynamically fetch metadata, descriptions, and cover images.
Book cards with detailed information, genres, and a list of authors.

### Personal Profile and Shelves:
- Creating personal collections ("Currently Reading", "Want to Read", "Favorites").
- Adding and removing books from collections.
- User search activity history.
- Writing reviews and submitting user ratings.

### Embedded Reader:
- Integration with Internet Archive API for access to open digitized editions.
- Reading book contents via an embedded reader (reader.html) without the need to manually download large files to the device.

## 🛠 Technology Stack
* Backend: Java 17+, Spring Boot 3 (Spring Web, Spring Data JPA, Spring Security, Spring Mail).
* Database: PostgreSQL, Docker / Docker Compose.
* Frontend: HTML5, CSS3, Thymeleaf.
* External APIs: OpenLibrary REST API, Internet Archive API.
* Build & Dependencies: Apache Maven.

## 📦 Quick Start and Local Setup
1. Environment Requirements
Installed Docker and Docker Compose.
Java Development Kit (JDK) 17 or newer.
Git.

2. Cloning and Setup
Clone the repository to your local machine:
git clone https://github.com/MelnykRoman6/BookNest_2.git
cd BookNest_2

4. Starting the Database
Start the PostgreSQL database container via Docker:
docker compose up -d

5. Configuration Parameters
Check the database and mail server connection parameters in src/main/resources/application.properties:

## Properties
### PostgreSQL Database
spring.datasource.url=jdbc:postgresql://localhost:5432/booknest
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update

### Mail settings for account confirmation
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=your-email@example.com
spring.mail.password=your-app-password

5. Building and Running the Application
Run the project via Maven Wrapper:
./mvnw spring-boot:run
(For Windows: mvnw.cmd spring-boot:run)




