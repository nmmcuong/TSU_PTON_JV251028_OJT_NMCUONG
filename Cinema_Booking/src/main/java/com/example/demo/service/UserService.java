package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.UserProfileDto;
import com.example.demo.dto.UserRegisterDto;
import com.example.demo.model.User;

public interface UserService {
    User registerNewUser(UserRegisterDto registerDto);
    User updateUserProfile(String username, UserProfileDto profileDto);
    List<User> getAllUsers();
    void adminUpdateUser(Long id, String fullName, String email, String phone, String role, Boolean enabled);
    void deleteUser(Long id);
}

