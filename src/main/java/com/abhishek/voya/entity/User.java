package com.abhishek.voya.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(unique = true)
    private String googleId;

    private String interests;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('admin', 'customer') DEFAULT 'customer'")
    private Role role;

    public enum Role {
        admin, customer
    }
}
