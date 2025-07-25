package com.daham.producer.database;

import com.daham.producer.domain.Measurement;
import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Dependent
@Transactional
@SuppressWarnings("all")
public class MeasurementDao {
  private static final int DEFAULT_MAX_RESULTS = 50;

  @PersistenceContext
  private EntityManager entityManager;
  private List<Measurement> cachedMeasurements;

  public boolean isEmpty(int maxResults) {
    try {
      if (cachedMeasurements == null || cachedMeasurements.isEmpty()) {
        cachedMeasurements = findAll(maxResults);
      }
      return cachedMeasurements.isEmpty();
    } catch (Exception ignore) {
      return false;
    }
  }

  public boolean isEmpty() {
    return isEmpty(DEFAULT_MAX_RESULTS);
  }

  public List<Measurement> findAll(int maxResults) throws DataAccessException {
    try {
      List<Measurement> measurements;
      if (cachedMeasurements != null && !cachedMeasurements.isEmpty()) {
        measurements = cachedMeasurements;
      }
      else {
        measurements = entityManager.createQuery("SELECT m FROM Measurement m", Measurement.class)
            .setMaxResults(maxResults)
            .getResultList();
      }
      return measurements;
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.getMessage());
    }
  }

  public List<Measurement> findAll() throws DataAccessException {
    return findAll(DEFAULT_MAX_RESULTS);
  }

  public void persist(Measurement measurement) throws DataAccessException {
    try {
      entityManager.merge(measurement);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.getMessage());
    }
  }

  public void removeAllCached() throws DataAccessException {
    try {
      for (var measurement : cachedMeasurements) {
        entityManager.remove(measurement);
      }
      cachedMeasurements.clear();
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.getMessage());
    }
  }

  public void removeAllOutdated(LocalDateTime maxTimestamp) throws DataAccessException {
    try {
      entityManager.createQuery("DELETE FROM Measurement m WHERE m.timestamp < :maxTimestamp")
          .setParameter("maxTimestamp", maxTimestamp)
          .executeUpdate();
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.getMessage());
    }
  }
}
