package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @GetMapping("/tickets")
    public String staffTicketManagement() {
        return "staff/tickets"; // Trỏ vào thư mục templates/staff/tickets.html
    }
}