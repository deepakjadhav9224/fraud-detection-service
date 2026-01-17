# Fraud Detection Service

Fraud Detection Service of GM Bank. This Spring Boot application is designed to detect fraudulent activities.

## Technologies Used

*   **Java 17**
*   **Spring Boot** (Web MVC, Data JPA)
*   **MySQL** (Database)
*   **Lombok** (Boilerplate code reduction)
*   **SpringDoc OpenAPI** (API Documentation)
*   **Maven** (Build tool)

## Prerequisites

*   Java Development Kit (JDK) 17 or later
*   Maven
*   MySQL Server

## Setup and Installation

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd fraud-detection
    ```

2.  **Configure the Database:**
    Update the `src/main/resources/application.properties` (or `application.yml`) file with your MySQL database credentials.
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name
    spring.datasource.username=your_username
    spring.datasource.password=your_password
    ```

3.  **Build the project:**
    ```bash
    ./mvnw clean install
    ```

4.  **Run the application:**
    ```bash
    ./mvnw spring-boot:run
    ```

## API Documentation

The application uses SpringDoc OpenAPI for API documentation. Once the application is running, you can access the Swagger UI at:

`http://localhost:8080/swagger-ui.html` (Port may vary based on configuration)
