package com.daham.core.database;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("all")
public interface Dao<T, ID extends Serializable> {
  boolean exists(ID id) throws DataAccessException;
  Optional<T> findById(ID id) throws DataAccessException;
  List<T> findAll() throws DataAccessException;
  T merge(T t) throws DataAccessException;
  void persist(T t) throws DataAccessException;
  void remove(T t) throws DataAccessException;
}
