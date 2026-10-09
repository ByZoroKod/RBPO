package org.example.service;

import org.example.dto.UserRequest;
import org.example.exception.NotFoundException;
import org.example.exception.ValidationException;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CollectibleItemService collectibleItemService;
    private final BidService bidService;

    public UserService(UserRepository userRepository,
                       CollectibleItemService collectibleItemService,
                       BidService bidService) {
        this.userRepository = userRepository;
        this.collectibleItemService = collectibleItemService;
        this.bidService = bidService;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: id=" + id));
    }

    public User create(UserRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("Не указано имя пользователя");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        return userRepository.save(user);
    }

    public User update(Long id, UserRequest request) {
        User user = getById(id);

        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw new ValidationException("Имя не может быть пустым");
            }
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        return userRepository.save(user);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь не найден: id=" + id);
        }
        collectibleItemService.deleteByOwner(id);
        bidService.deleteByBidder(id);
        userRepository.deleteById(id);
    }
}
