package org.example.model.dao;

import java.util.List;

public interface GenericDAO<T, ID> {
    T save(T entity); // Consider returning the managed entity
    T findById(ID id);
    List<T> findAll();
    void delete(T entity); // Changed signature to accept entity object
}

