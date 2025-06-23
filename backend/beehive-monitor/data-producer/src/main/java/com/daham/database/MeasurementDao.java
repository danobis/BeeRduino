package com.daham.database;

import com.daham.domain.Measurement;
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
public class MeasurementDao {
  private static final int DEFAULT_MAX_RESULTS = 50;

  @PersistenceContext
  private EntityManager entityManager;
  private List<Measurement> cachedMeasurements;

  public boolean isEmpty() {
    try {
      if (cachedMeasurements == null || cachedMeasurements.isEmpty()) {
        cachedMeasurements = findAll(DEFAULT_MAX_RESULTS);
      }
      return cachedMeasurements.isEmpty();
    } catch (Exception ignore) {
      return false;
    }
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
      throw new DataAccessException(e.toString());
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
      throw new DataAccessException(e.toString());
    }
  }

  public void removeAll(LocalDateTime maxTimestamp) throws DataAccessException {
    try {
      entityManager.createQuery("DELETE FROM Measurement m WHERE m.timestamp < :maxTimestamp")
          .setParameter("maxTimestamp", maxTimestamp)
          .executeUpdate();
    } catch (PersistenceException e) {
      log.error("DataAccessException, ERROR", e);
      throw new DataAccessException(e.toString());
    }
  }
}
