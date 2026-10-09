package org.example.repository;

import org.example.model.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class InMemoryRepository<T extends BaseEntity> {

    private final Map<Long, T> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<T> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    public boolean existsById(Long id) {
        return id != null && store.containsKey(id);
    }

    public T save(T entity) {
        if (entity.getId() == null) {
            entity.setId(sequence.incrementAndGet());
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    public boolean deleteById(Long id) {
        return id != null && store.remove(id) != null;
    }

    protected List<T> findBy(Predicate<T> predicate) {
        return store.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }
}

