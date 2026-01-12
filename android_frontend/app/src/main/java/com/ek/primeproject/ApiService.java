package com.ek.primeproject;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("/api/products")
    Call<List<Product>> getProducts();

    @GET("/api/cart")
    Call<List<CartItem>> getCart();

    @GET("/api/orders/my-orders")
    Call<List<Order>> getOrders();

    @GET("/api/auth/me")
    Call<User> getMe();

    @retrofit2.http.PUT("/api/auth/profile")
    Call<Void> updateProfile(@Body User user);
}
