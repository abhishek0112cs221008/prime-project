package com.ek.primeproject;

public class User {
    private Integer id;
    private String name;
    private String email;
    private String interests; // Comma separated string

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getInterests() {
        return interests;
    }

    // Setters for updating
    public void setName(String name) {
        this.name = name;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    // Constructor matching the update requirements
    public User(String name, String interests) {
        this.name = name;
        this.interests = interests;
    }
}
