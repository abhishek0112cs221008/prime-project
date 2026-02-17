# System Diagrams

## 1. Class Diagram
This diagram illustrates the high-level object-oriented structure of the backend, showing how Controllers, Services, Repositories, and Entities interact.

```mermaid
classDiagram
    direction TB

    %% Controllers
    class AuthController {
        +register(request)
        +login(request)
        +googleLogin(request)
        +logout(token)
    }
    class ProductController {
        +getAllProducts()
        +getProductById(id)
        +createProduct(product)
        +updateProduct(id, product)
        +deleteProduct(id)
    }
    class OrderController {
        +createOrder(request)
        +getUserOrders(email)
        +getAllOrders()
        +verifyOrder(id)
    }
    class CartController {
        +getCart(token)
        +addToCart(request)
        +removeFromCart(id)
    }

    %% Services
    class AuthService {
        +register(request)
        +login(request)
        +resetPassword(request)
    }
    class ProductService {
        +findAll()
        +findById(id)
        +save(product)
        +delete(id)
    }
    class OrderService {
        +placeOrder(request)
        +verifyPayment(id)
    }
    class CartService {
        +getCart(email)
        +addToCart(email, request)
        +removeFromCart(cartId)
    }
    class SessionService {
        +createSession(user)
        +getUserId(token)
    }

    %% Repositories
    class UserRepository {
        +findByEmail(email)
    }
    class ProductRepository {
        +findByCategory(category)
    }
    class OrderRepository {
        +findByUserEmail(email)
    }
    class CartItemRepository {
        +findByUserEmail(email)
    }
    class PasswordResetTokenRepository {
        +findByToken(token)
    }

    %% Entities
    class User {
        -Integer id
        -String email
        -String password
        -Role role
    }
    class Product {
        -Integer id
        -String name
        -BigDecimal price
        -String category
    }
    class Order {
        -Integer id
        -String userEmail
        -String status
        -BigDecimal discount
    }
    class CartItem {
        -Integer id
        -String userEmail
        -Integer quantity
    }
    class PasswordResetToken {
        -Long id
        -String token
        -LocalDateTime expiryDate
    }

    %% Relationships
    AuthController ..> AuthService : uses
    AuthController ..> SessionService : uses
    ProductController ..> ProductService : uses
    OrderController ..> OrderService : uses
    CartController ..> CartService : uses

    AuthService ..> UserRepository : uses
    AuthService ..> PasswordResetTokenRepository : uses
    ProductService ..> ProductRepository : uses
    OrderService ..> OrderRepository : uses
    OrderService ..> ProductRepository : uses
    CartService ..> CartItemRepository : uses
    CartService ..> ProductRepository : uses

    UserRepository ..> User : returns
    ProductRepository ..> Product : returns
    OrderRepository ..> Order : returns
    CartItemRepository ..> CartItem : returns
    PasswordResetTokenRepository ..> PasswordResetToken : returns

    PasswordResetToken "1" *-- "1" User : has
    Order "*" --> "1" Product : has
    CartItem "*" --> "1" Product : has
```

## 2. Entity Relationship Diagram (ERD)
This diagram represents the database schema and the relationships between tables. Note that `orders`, `reviews`, and `cart_items` often reference `users` logically via `user_email` rather than a strict foreign key to `user_id`.

```mermaid
erDiagram
    users {
        int id PK
        string email UK "Unique"
        string password
        string name
        string role "ENUM('admin', 'customer')"
        string google_id UK
        string interests
    }

    products {
        int id PK
        string name
        string category
        decimal price
        int quantity
        string image_url
        text description
        string git_repo_url
        text installation_guide
        string discount_code
        decimal discount_percent
        int view_count
    }

    orders {
        int id PK
        string user_email "Logical Ref to users.email"
        string shipping_address
        string utr
        boolean is_verified
        decimal discount
        int product_id FK
        int quantity
        datetime order_date
        string payment_id
        string status
        string rejection_reason
    }

    reviews {
        int id PK
        int product_id "Logical Ref to products.id"
        string user_email "Logical Ref to users.email"
        int rating
        string comment
        datetime created_at
    }

    cart_items {
        int id PK
        string user_email "Logical Ref to users.email"
        int product_id FK
        int quantity
        datetime added_date
    }

    password_reset_tokens {
        long id PK
        string token UK
        int user_id FK
        datetime expiry_date
    }

    %% Relationships
    products ||--o{ orders : "is in"
    products ||--o{ cart_items : "is in"
    users ||--o{ password_reset_tokens : "has"
    
    %% Logical Relationships (Documented as dotted lines or comments in Mermaid)
    users ||..o{ orders : "places (via email)"
    users ||..o{ cart_items : "owns (via email)"
    users ||..o{ reviews : "writes (via email)"
    products ||..o{ reviews : "has (via id)"
```
