# System Architecture: VOYA (Prime Project)

## 1. Project Overview

**VOYA** is a full-stack e-commerce web application designed to sell digital projects and assets. It features a modern, responsive user interface and a robust Spring Boot backend. The system supports user authentication (including Google Login), product management, shopping cart functionality, order processing, and an admin dashboard.

## 2. Technology Stack

### Frontend

- **Languages**: HTML5, CSS3, JavaScript (ES6+)
- **Framework**: Vanilla (No heavy JS frameworks like React/Vue used)
- **Styling**: Custom CSS (Variables, Flexbox, Grid), Glassmorphism aesthetics
- **Icons**: SVG Icons (inline and feather-icons style)
- **Fonts**: Google Fonts (Inter)

### Backend

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.2
- **Build Tool**: Maven
- **Dependencies**:
  - `spring-boot-starter-web` (REST API)
  - `spring-boot-starter-data-jpa` (ORM)
  - `spring-boot-starter-validation` (Data validation)
  - `spring-boot-starter-mail` (Email notifications)
  - `google-api-client` (Google Auth)
  - `lombok` (Boilerplate reduction)
  - `spring-dotenv` (Environment variable management)

### Database

- **RDMS**: MySQL
- **ORM**: Hibernate (via Spring Data JPA)

### Deployment / Environment

- **Local Address**: `http://192.168.1.3:8080` (Configured for LAN access)
- **Configuration**: Environment variables via `.env` file

## 3. High-Level Architecture Diagram

```mermaid
graph TD
    Client[Browser / Client]
  
    subgraph Frontend [Frontend Layer]
        HTML[HTML Pages]
        CSS[Custom CSS]
        JS[api.js / logic]
    end
  
    subgraph Backend [Backend Layer (Spring Boot)]
        Controller[Controllers (REST API)]
        Service[Service Layer (Business Logic)]
        Repo[Repository Layer (JPA)]
        Security[Auth & Session Management]
    end
  
    subgraph Data [Data Layer]
        DB[(MySQL Database)]
    end
  
    Client -->|HTTP/HTTPS| Frontend
    Frontend -->|REST API (JSON)| Controller
    Controller --> Service
    Service --> Repo
    Repo -->|JDBC| DB
```

## 4. Directory Structure

The project follows a standard Maven project structure with a dedicated `frontend` directory for static assets.

```text
prime project/
├── .env                       # Environment variables (DB creds, API keys)
├── pom.xml                    # Maven dependencies
├── frontend/                  # Frontend Source Code
│   ├── css/                   # Stylesheets (style.css)
│   ├── js/                    # JavaScript logic (api.js, watch.js)
│   ├── images/                # Static images
│   ├── index.html             # Landing page
│   ├── login.html             # Auth page
│   ├── register.html          # Registration page
│   ├── dashboard.html         # User/Admin dashboard
│   ├── cart.html              # Shopping cart
│   └── ...                    # Other HTML pages
└── src/
    └── main/
        ├── java/com/abhishek/voya/
        │   ├── PrimeProjectApplication.java # Entry point
        │   ├── controller/    # REST Endpoints
        │   ├── service/       # Business Logic
        │   ├── repository/    # Data Access Interfaces
        │   ├── entity/        # Database Models
        │   ├── dto/           # Data Transfer Objects
        │   ├── config/        # App Configurations
        │   ├── exception/     # Global Exception Handling
        │   └── component/     # Utility Components
        └── resources/
            └── application.properties # Spring Boot Config
```

## 5. Backend Architecture Details

### *A*PI Layer (Controllers)

The backend exposes a RESTful API. Controllers handle HTTP requests, validate input using DTOs, and return `ResponseEntity` objects.

- **`AuthController`**: Handles `/api/auth` (Register, Login, Google Login, Logout, Profile).
- **`ProductController`**: Manages product CRUD operations.
- **`OrderController`**: Handles order creation and verification.
- **`CartController`**: Manages user shopping cart.
- **`DashboardController`**: Admin dashboard statistics.
- **`ReviewController`**: Product reviews and ratings.

### Business Logic *(Services)*

Services contain the core application logic and transaction management.

- **`AuthService`**: User registration, login logic, password reset.
- **`SessionService`**: Manages custom session tokens.
- **`ProductService`**, **`OrderService`**, etc.

### Data Layer (Entities & Repositories)

Uses Spring Data JPA for database interactions.

- **`User` Entity**:
  - Fields: `id`, `name`, `email`, `password` (hashed), `role` (ENUM: admin, customer), `googleId`.
- **Other Entities**: `Product`, `Order`, `Review`, `Cart`.
- **Repositories**: Interfaces extending `JpaRepository` (e.g., `UserRepository`, `ProductRepository`).

### Authentication & Security

- **Mechanism**: Custom Token-based Authentication.
- **Flow**:
  1. User logs in.
  2. Backend validates credentials and generates a token.
  3. Frontend stores token in `localStorage`.
  4. Frontend sends token in `X-Auth-Token` header for protected requests.
- **Session Management**: Handled by `SessionService` (likely storing active tokens in DB or memory).
- **Password Reset**: Token-based email flow (`PasswordResetTokenRepository`).

## 6. Frontend Architecture Details

### Structure

- **Multi-Page Application (MPA)**: Each view is a separate HTML file (`index.html`, `login.html`).
- **Shared Logic**: `js/api.js` acts as the central networking layer.
- **Styling**: `css/style.css` contains global styles, theming (dark/light mode), and component styles.

### Key Components

- **`Api` Class (`js/api.js`)**:
  - Static configuration `API_BASE` pointing to the backend.
  - Methods: `get`, `post`, `put`, `delete`.
  - Automatically attaches `X-Auth-Token` logic.
  - Handles global error states (e.g., 401 Unauthorized redirects to login).
- **State Management**:
  - **User Session**: Stored in `localStorage` (`user` object contains `token` + user details).
  - **Theme**: Stored in `localStorage` (`theme` = 'dark' | 'light').
- **Admin Panel**:
  - Hidden by default.
  - Rendered via JavaScript on `index.html` (and others) if `user.role === 'admin'`.

## 7. Database Design (Inferred Schema)

### Tables

- **`users`**: User accounts (admin/customer).
- **`products`**: Items for sale (name, price, image, description, category).
- **`orders`**: purchase records (user_id, product_id, status, payment_id).
- **`reviews`**: User feedback on products.
- **`password_reset_tokens`**: Temporary tokens for password recovery.
- **`carts`** / **`cart_items`**: Temporary product storage for users.

## 8. Deployment & Environment

- **Configuration**: The application uses `spring-dotenv` to load configuration from a `.env` file at the project root.
- **Key Variables**:
  - `SPRING_DATASOURCE_URL`, `USERNAME`, `PASSWORD`
  - `SPRING_MAIL_USERNAME`, `PASSWORD` (For emails)
  - `GOOGLE_CLIENT_ID` (For OAuth)
- **Local Execution**: The frontend connects to the backend via a hardcoded IP in `api.js` (`http://192.168.1.3:8080/api`), facilitating testing on local network devices (e.g., mobile phones).
