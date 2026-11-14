package org.example.service;

import org.example.dto.RegisterRequest;
import org.example.dto.UserRequest;
import org.example.exception.ConflictException;
import org.example.exception.NotFoundException;
import org.example.exception.ValidationException;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.security.AppUserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_.-]{3,50}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: id=" + id));
    }

    public User register(RegisterRequest request) {
        UserRequest userRequest = new UserRequest();
        userRequest.setName(request.getName());
        userRequest.setEmail(request.getEmail());
        userRequest.setUsername(request.getUsername());
        userRequest.setPassword(request.getPassword());
        return create(userRequest);
    }

    public User create(UserRequest request) {
        requireName(request.getName());
        String username = request.getUsername();
        if (username == null || !USERNAME.matcher(username).matches()) {
            throw new ValidationException("Логин: от 3 до 50 символов, латинские буквы, цифры, '_', '.', '-'");
        }
        PasswordPolicy.validate(request.getPassword(), username);
        String email = checkedEmail(request.getEmail());

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Логин уже занят");
        }
        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Пользователь с таким e-mail уже существует");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        return userRepository.save(user);
    }

    public User update(Long id, UserRequest request, AppUserDetails actor) {
        User user = getById(id);

        if ((request.getRole() != null || request.getStatus() != null) && actor.getId().equals(id)) {
            throw new ConflictException("Нельзя менять собственную роль или статус");
        }
        if (request.getName() != null) {
            requireName(request.getName());
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            String email = checkedEmail(request.getEmail());
            if (email != null && !email.equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmailIgnoreCase(email)) {
                throw new ConflictException("Пользователь с таким e-mail уже существует");
            }
            user.setEmail(email);
        }
        if (request.getPassword() != null) {
            PasswordPolicy.validate(request.getPassword(), user.getUsername());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        return userRepository.save(user);
    }

    public void delete(Long id, AppUserDetails actor) {
        if (actor.getId().equals(id)) {
            throw new ConflictException("Нельзя удалить собственную учётную запись");
        }
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь не найден: id=" + id);
        }
        userRepository.deleteById(id);
    }

    private void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Не указано имя пользователя");
        }
    }

    private String checkedEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new ValidationException("Некорректный e-mail");
        }
        return email;
    }
}
