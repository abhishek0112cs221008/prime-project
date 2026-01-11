package com.abhishek.voya.component;

import com.abhishek.voya.entity.User;
import com.abhishek.voya.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

        @Autowired
        private UserRepository userRepository;

        @Override
        public void run(String... args) throws Exception {
                // Removed dummy project initialization as per request.

                // Ensure Admin User Exists
                if (!userRepository.existsByEmail("admin@voya.com")) {
                        User admin = new User();
                        admin.setName("Admin User");
                        admin.setEmail("admin@voya.com");
                        admin.setPassword("admin123"); // Ideally hashed if AuthService uses hashing
                        admin.setRole(User.Role.admin);
                        userRepository.save(admin);
                        System.out.println("Default Admin User Created: admin@voya.com / admin123");
                }
        }
}
