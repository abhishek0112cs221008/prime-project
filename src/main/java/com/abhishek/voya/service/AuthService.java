package com.abhishek.voya.service;

import com.abhishek.voya.dto.DTOs;
import com.abhishek.voya.entity.User;
import com.abhishek.voya.exception.BadRequestException;
import com.abhishek.voya.exception.ResourceNotFoundException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.abhishek.voya.repository.UserRepository;
import com.abhishek.voya.repository.PasswordResetTokenRepository;
import com.abhishek.voya.entity.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @Value("${app.frontend.url:http://192.168.1.3:8080}")
    private String frontendUrl;

    public User loginWithGoogle(String idTokenString) {
        GoogleIdToken idToken = verifyGoogleToken(idTokenString);
        if (idToken == null) {
            throw new BadRequestException("Invalid Google token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");

        return userRepository.findByEmail(email).map(user -> {
            user.setGoogleId(googleId);
            return userRepository.save(user);
        }).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setGoogleId(googleId);
            user.setRole(User.Role.customer);
            user.setPassword("GOOGLE_OAUTH_USER"); // Placeholder
            return userRepository.save(user);
        });
    }

    private GoogleIdToken verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            return verifier.verify(idTokenString);
        } catch (Exception e) {
            return null;
        }
    }

    public User register(DTOs.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(User.Role.customer);
        return userRepository.save(user);
    }

    public User login(DTOs.LoginRequest request) {
        if ("admin@mail.com".equals(request.getEmail()) && "admin#voya".equals(request.getPassword())) {
            return userRepository.findByEmail("admin@mail.com").orElseGet(() -> {
                // Create admin if not exists, or handle as special session
                // Assuming admin is in DB or we just return a stub if really needed
                // Let's rely on DB having it (user said DB is VOYA.sql)
                throw new ResourceNotFoundException("Admin user not found in DB");
            });
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }
        return user;
    }

    public User updateProfile(Integer userId, DTOs.UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setName(request.getName());
        user.setInterests(request.getInterests());
        return userRepository.save(user);
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public void forgotPassword(DTOs.ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this email"));

        // Delete existing tokens for this user
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password.html?token=" + token;
        String emailBody = "<h1>Password Reset Request</h1>" +
                "<p>Click the link below to reset your password:</p>" +
                "<a href=\"" + resetLink + "\">Reset Password</a>" +
                "<p>This link will expire in 24 hours.</p>";

        emailService.sendEmail(user.getEmail(), "Reset Your Password - Prime Project", emailBody);
    }

    @Transactional
    public void resetPassword(DTOs.ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));

        if (token.isExpired()) {
            tokenRepository.delete(token);
            throw new BadRequestException("Token has expired");
        }

        User user = token.getUser();
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
        tokenRepository.delete(token);
    }

    public void changePassword(Integer userId, DTOs.ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(request.getNewPassword());
        userRepository.save(user);
    }
}
