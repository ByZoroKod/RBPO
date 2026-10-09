package org.example.repository;

import org.example.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends InMemoryRepository<User> {
}

