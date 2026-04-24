package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users") // All links here start with /api/users
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping // This handles SAVING data
    public User saveUser(@RequestBody User user) {
        return userService.createNewUser(user);
    }

    @GetMapping // This handles GETTING data
    public List<User> getUsers() {
        return userService.findAll();
    }
}