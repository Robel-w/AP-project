package com.civicpulse.backend.service;

import com.civicpulse.backend.dto.LoginRequest;
import com.civicpulse.backend.dto.RegisterRequest;
import com.civicpulse.backend.model.User;
import com.civicpulse.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.civicpulse.backend.model.UserRole;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    public String register(RegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already Registerd!");
        }
        if(userRepository.existsByUsername(request.getUsername())){
            throw new RuntimeException("username already taken");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(UserRole.USER); // Enforce USER role for public registration

        userRepository.save(user);
        return "User registered Successfully!";
    }
    public User login(LoginRequest request){
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if(userOpt.isEmpty()){
            throw new RuntimeException("Invalid Email or password!");
        }
        User user = userOpt.get();
        if(!user.getPassword().equals(request.getPassword())){
            throw new RuntimeException("Invalid password!");
        }
        return user;
    }
    public String registerAdmin(RegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already Registerd!");
        }
        if(userRepository.existsByUsername(request.getUsername())){
            throw new RuntimeException("username already taken");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(UserRole.ADMIN); // Enforce ADMIN role

        userRepository.save(user);
        return "Admin registered Successfully!";
    }
}
