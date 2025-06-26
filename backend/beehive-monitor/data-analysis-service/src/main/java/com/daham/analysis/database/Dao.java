package com.daham.analysis.database;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object (DAO) interface for managing persistent entities.
 * <p>
 * Provides basic CRUD operations for any entity type {@code T} identified by a primary key {@code ID}.
 * Intended to be extended or implemented by concrete DAO classes, typically using JPA.
 * </p>
 *
 * <p>
 * All methods throw {@link DataAccessException} in case of persistence-related errors.
 * </p>
 *
 * @param <T>  The type of the entity.
 * @param <ID> The type of the entity identifier (must be {@link Serializable}).
 *
 * @author
 * @version 1.0
 */
@SuppressWarnings("all")
public interface Dao<T, ID extends Serializable> {
  /**
   * Checks whether an entity with the given ID exists in the database.
   *
   * @param id The identifier of the entity.
   * @return {@code true} if the entity exists, {@code false} otherwise.
   * @throws DataAccessException If a persistence error occurs.
   */
  boolean exists(ID id) throws DataAccessException;

  /**
   * Retrieves an entity by its ID.
   *
   * @param id The identifier of the entity.
   * @return An {@link Optional} containing the entity if found, or empty if not found.
   * @throws DataAccessException If a persistence error occurs.
   */
  Optional<T> findById(ID id) throws DataAccessException;

  /**
   * Retrieves all entities of type {@code T} from the database.
   *
   * @return A list of all entities.
   * @throws DataAccessException If a persistence error occurs.
   */
  List<T> findAll() throws DataAccessException;

  /**
   * Updates the state of the given entity in the persistence context.
   *
   * @param t The entity to merge.
   * @return The managed instance that the state was merged to.
   * @throws DataAccessException If a persistence error occurs.
   */
  T merge(T t) throws DataAccessException;

  /**
   * Persists the given new entity in the database.
   *
   * @param t The entity to persist.
   * @throws DataAccessException If a persistence error occurs.
   */
  void persist(T t) throws DataAccessException;

  /**
   * Removes the given entity from the database.
   *
   * @param t The entity to remove.
   * @throws DataAccessException If a persistence error occurs.
   */
  void remove(T t) throws DataAccessException;
}
