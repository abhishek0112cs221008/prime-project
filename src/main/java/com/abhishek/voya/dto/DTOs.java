package com.abhishek.voya.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

public class DTOs {

    @Data
    public static class LoginRequest {
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String name;
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class ProductRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String category;
        @NotNull
        @Positive
        private BigDecimal price;
        @NotNull
        @Positive
        private Integer quantity;
        private String imageUrl;
        private String description;
        private String gitRepoUrl;
        private String installationGuide;
        private String discountCode;
        private java.math.BigDecimal discountPercentage;
    }

    @Data
    public static class CartRequest {
        @jakarta.validation.constraints.NotNull
        private Integer productId;
        @jakarta.validation.constraints.Min(1)
        private Integer quantity;
    }

    @Data
    public static class OrderRequest {
        @jakarta.validation.constraints.NotBlank
        private String shippingAddress;
        @jakarta.validation.constraints.NotBlank
        private String utr;
        private String discountCode;
    }

    @Data
    public static class RejectOrderRequest {
        @NotBlank
        private String reason;
    }

    @Data
    public static class UpdateProfileRequest {
        @NotBlank
        private String name;
        private String interests;
    }

    @Data
    public static class ForgotPasswordRequest {
        @NotBlank
        @Email
        private String email;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank
        private String token;
        @NotBlank
        private String newPassword;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String oldPassword;
        @NotBlank
        private String newPassword;
    }
}
