package com.daham.core.database;

import com.daham.core.domain.Location;
import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Dependent
@Transactional
public class LocationDaoBean implements LocationDao {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public boolean exists(UUID id) throws DataAccessException {
    return findById(id).isPresent();
  }

  @Override
  public Optional<Location> findById(UUID id) throws DataAccessException {
    try {
      return Optional.ofNullable(entityManager.find(Location.class, id));
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public List<Location> findAll() throws DataAccessException {
    try {
      return entityManager.createQuery("SELECT l FROM Location l", Location.class).getResultList();
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public Location merge(Location location) throws DataAccessException {
    try {
      return entityManager.merge(location);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public void persist(Location location) throws DataAccessException {
    try {
      entityManager.persist(location);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public void remove(Location location) throws DataAccessException {
    try {
      entityManager.remove(location);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }
}
