package com.mitocode.service;

import java.util.List;

public interface ICRUD<T, ID> {

    List<T> findAll();

    T save(T t);

    T findById(ID id);

    T update(ID id, T t);

    void delete(ID id);
}
