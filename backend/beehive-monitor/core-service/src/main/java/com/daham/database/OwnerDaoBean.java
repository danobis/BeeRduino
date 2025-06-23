package com.daham.database;

import com.daham.domain.Owner;
import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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
public class OwnerDaoBean implements OwnerDao {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public boolean exists(UUID id) throws DataAccessException {
    return findById(id).isPresent();
  }

  @Override
  public Optional<Owner> findById(UUID id) throws DataAccessException {
    try {
      return Optional.ofNullable(entityManager.find(Owner.class, id));
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public List<Owner> findAll() throws DataAccessException {
    try {
      return entityManager.createQuery("SELECT o FROM Owner o", Owner.class).getResultList();
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public Owner merge(Owner owner) throws DataAccessException {
    try {
      return entityManager.merge(owner);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public void persist(Owner owner) throws DataAccessException {
    try {
      entityManager.persist(owner);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public void remove(Owner owner) throws DataAccessException {
    try {
      entityManager.remove(owner);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public Optional<Owner> findOwnerByEmail(String email) throws DataAccessException {
    try {
      return Optional.ofNullable(entityManager
          .createQuery("SELECT o FROM Owner o WHERE o.email = :email", Owner.class)
          .setParameter("email", email)
          .getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.toString());
    }
  }
}
