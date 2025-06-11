package com.daham.core.database;

import com.daham.core.domain.Beehive;
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
public class BeehiveDaoBean implements BeehiveDao {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public boolean exists(UUID id) throws DataAccessException {
    return findById(id).isPresent();
  }

  @Override
  public Optional<Beehive> findById(UUID id) throws DataAccessException {
    try {
      return Optional.ofNullable(entityManager.find(Beehive.class, id));
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public List<Beehive> findAll() throws DataAccessException {
    try {
      return entityManager.createQuery("SELECT b FROM Beehive b", Beehive.class).getResultList();
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public Beehive merge(Beehive beehive) throws DataAccessException {
    try {
      return entityManager.merge(beehive);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public void persist(Beehive beehive) throws DataAccessException {
    try {
      entityManager.persist(beehive);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public void remove(Beehive beehive) throws DataAccessException {
    try {
      entityManager.remove(beehive);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public List<Beehive> findAllByOwnerId(UUID ownerId) throws DataAccessException {
    try {
      return entityManager
          .createQuery("SELECT b FROM Beehive b WHERE b.ownerId = :ownerId", Beehive.class)
          .setParameter("ownerId", ownerId)
          .getResultList();
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public Optional<Beehive> findByOwnerIdAndBeehiveId(UUID ownerId, UUID beehiveId) throws DataAccessException {
    try {
      final String hql = """
          SELECT b
          FROM Beehive b
          WHERE b.ownerId = :ownerId
            AND b.id      = :beehiveId
          """;
      return Optional.ofNullable(entityManager
          .createQuery(hql, Beehive.class)
          .setParameter("ownerId", ownerId)
          .setParameter("beehiveId", beehiveId)
          .getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }
}
