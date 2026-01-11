package com.abhishek.voya.service;

import com.abhishek.voya.entity.User;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
public class SessionService {
    private final Map<String, Integer> sessions = new ConcurrentHashMap<>();

    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getId());
        return token;
    }

    public Integer getUserId(String token) {
        if (token == null)
            return null;
        return sessions.get(token);
    }

    public void invalidateSession(String token) {
        if (token != null)
            sessions.remove(token);
    }
}
