package com.daham.analysis.database;

import com.daham.analysis.domain.RegistryEntry;
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
public class BeehiveRegistryDaoBean implements BeehiveRegistryDao {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public boolean exists(UUID id) throws DataAccessException {
    return findById(id).isPresent();
  }

  @Override
  public Optional<RegistryEntry> findById(UUID id) throws DataAccessException {
    try {
      return Optional.ofNullable(entityManager.find(RegistryEntry.class, id));
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.getMessage());
    }
  }

  @Override
  public List<RegistryEntry> findAll() throws DataAccessException {
    try {
      return entityManager.createQuery("SELECT r FROM RegistryEntry r", RegistryEntry.class).getResultList();
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.getMessage());
    }
  }

  @Override
  public RegistryEntry merge(RegistryEntry entry) throws DataAccessException {
    try {
      return entityManager.merge(entry);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.getMessage());
    }
  }

  @Override
  public void persist(RegistryEntry entry) throws DataAccessException {
    try {
      entityManager.persist(entry);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.getMessage());
    }
  }

  @Override
  public void remove(RegistryEntry entry) throws DataAccessException {
    try {
      entityManager.remove(entry);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.getMessage());
    }
  }
}
