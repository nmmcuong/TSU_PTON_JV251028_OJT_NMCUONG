package com.example.demo.service;

import com.example.demo.dto.UserRegisterDto;
import com.example.demo.model.User;

public interface UserService {
    User registerNewUser(UserRegisterDto registerDto);
}