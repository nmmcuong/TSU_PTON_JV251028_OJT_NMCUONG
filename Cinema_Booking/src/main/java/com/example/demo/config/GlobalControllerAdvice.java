package com.example.demo.config;

import com.example.demo.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("currentUser") 
    public User getCurrentUser(HttpSession session) {
       
        return (User) session.getAttribute("currentUser");
    }
}