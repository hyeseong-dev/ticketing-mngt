package com.mgnt.ticketing.controller;

import com.mgnt.ticketing.dto.users.UserCreateRequestDto;
import com.mgnt.ticketing.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping("/register")
    public String registerUser(@RequestBody UserCreateRequestDto request) {
//        return userService.registerUser(request.getEmail(), request.getPassword(), request.getName());
        return "success";
    }
}


