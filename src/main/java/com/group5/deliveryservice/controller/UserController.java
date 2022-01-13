package com.group5.deliveryservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.group5.deliveryservice.model.User;
import com.group5.deliveryservice.repository.UserRepository;
import com.group5.deliveryservice.service.SequenceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    private void checkEmailUniqueness(User user) throws RuntimeException {
        if (userRepository.findByEmail(user.getEmail()).isPresent())
            throw new RuntimeException("User with email " + user.getEmail() + " already exists");
    }

    private ResponseEntity<User> updateUser(User user1, User user2) {
        checkEmailUniqueness(user2);
        user1.setEmail(user2.getEmail());
        user1.setPassword(user2.getPassword());
        user1.setRole(user2.getRole());
        user1.setFirstName(user2.getFirstName());
        user1.setLastName(user2.getLastName());
        return ResponseEntity.ok(userRepository.save(user1));
    }

    public Map<String, Boolean> deleteUser(User user) {
        userRepository.delete(user);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }

    public static <T> ResponseEntity<T> getById(MongoRepository<T, Long> repository, Long id, String entityName)
            throws RuntimeException {
        T t = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(entityName + " not found for id " + id));
        return ResponseEntity.ok().body(t);
    }

    @GetMapping("/users/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable(value = "id") Long userId)
            throws RuntimeException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for id " + userId));
        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/users")
    public ResponseEntity<User> getUserByEmail(@RequestParam(value = "email") String email)
            throws RuntimeException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email " + email));
        return ResponseEntity.ok().body(user);
    }

    @PostMapping("/users")
    public User create(@Valid @RequestBody User user) {
        checkEmailUniqueness(user);
        user.setId(sequenceGeneratorService.generateSequence(User.SEQUENCE_NAME));
        return userRepository.save(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable(value = "id") Long userId,
                                           @Valid @RequestBody User userDetails) throws RuntimeException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for id " + userId));
        return updateUser(user, userDetails);
    }

    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@RequestParam(value = "email") String email,
                                           @Valid @RequestBody User userDetails) throws RuntimeException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email " + email));
        return updateUser(user, userDetails);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Boolean> deleteUser(@PathVariable(value = "id") Long userId)
            throws RuntimeException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for id " + userId));
        return deleteUser(user);
    }

    @DeleteMapping("/users")
    public Map<String, Boolean> deleteUser(@RequestParam(value = "email") String email)
            throws RuntimeException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email " + email));
        return deleteUser(user);
    }
}
