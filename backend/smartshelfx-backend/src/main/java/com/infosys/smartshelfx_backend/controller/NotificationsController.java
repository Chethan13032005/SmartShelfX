package com.infosys.smartshelfx_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications-compat")
public class NotificationsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static class Notification { public Long id; public String type; public String message; public Date createdAt; }

    @GetMapping
    public List<Notification> getAll() {
        String sql = "SELECT id, type, message, created_at FROM notifications ORDER BY created_at DESC LIMIT 200";
        return jdbcTemplate.query(sql, (rs, i) -> {
            Notification n = new Notification();
            n.id = rs.getLong("id");
            n.type = rs.getString("type");
            n.message = rs.getString("message");
            n.createdAt = rs.getTimestamp("created_at");
            return n;
        });
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, String> body) {
        String type = body.getOrDefault("type", "INFO");
        String message = body.getOrDefault("message", "");
        jdbcTemplate.update("INSERT INTO notifications (type, message, created_at) VALUES (?, ?, NOW())", type, message);
        return Collections.singletonMap("status", "ok");
    }
}
