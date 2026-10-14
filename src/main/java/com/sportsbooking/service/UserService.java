package com.sportsbooking.service;

import com.sportsbooking.model.Role;
import com.sportsbooking.model.User;
import com.sportsbooking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Registers a new player. Email must be unique (US1). */
    @Transactional
    public User register(User user) {
        String email = user.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BookingException("An account with this email already exists");
        }
        user.setEmail(email);
        user.setName(user.getName().trim());
        user.setRole(Role.PLAYER);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    public Optional<User> findById(Long id) {
        return id == null ? Optional.empty() : userRepository.findById(id);
    }
}
