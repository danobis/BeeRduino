package com.daham.core.database;

import com.daham.core.domain.Measurement;
import com.daham.core.domain.SensorType;
import com.daham.core.services.HistorySpan;
import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Dependent
@Transactional
public class MeasurementDaoBean implements MeasurementDao {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public boolean exists(UUID id) throws DataAccessException {
    return findById(id).isPresent();
  }

  @Override
  public Optional<Measurement> findById(UUID id) throws DataAccessException {
    try {
      return Optional.ofNullable(entityManager.find(Measurement.class, id));
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public List<Measurement> findAll() throws DataAccessException {
    try {
      return entityManager.createQuery("SELECT m FROM Measurement m", Measurement.class).getResultList();
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public Measurement merge(Measurement measurement) throws DataAccessException {
    try {
      return entityManager.merge(measurement);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public void persist(Measurement measurement) throws DataAccessException {
    try {
      entityManager.persist(measurement);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public void remove(Measurement measurement) throws DataAccessException {
    try {
      entityManager.remove(measurement);
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }

  @Override
  public List<Measurement> findAllByOwnerIdAndBeehiveId(UUID ownerId, UUID beehiveId, SensorType type, HistorySpan span) throws DataAccessException {
    try {
      final boolean haveType = SensorType.SENSOR_DEFAULT != type;
      var hqlBuilder = new StringBuilder("""
          SELECT m
          FROM Measurement m
          WHERE m.ownerId   = :ownerId
            AND m.beehiveId = :beehiveId
          """);
      if (haveType) {
        hqlBuilder.append("  AND m.type = :type\n");
      }
      var minTimestamp = LocalDateTime.now();
      minTimestamp = switch (span) {
        case HISTORY_LAST_5MINUTES -> minTimestamp.minusMinutes(5);
        case HISTORY_LAST_5HOURS -> minTimestamp.minusHours(5);
        case HISTORY_LAST_5DAYS -> minTimestamp.minusDays(5);
        default -> minTimestamp.minusSeconds(5);
      };
      hqlBuilder.append("  AND m.timestamp >= :minTimestamp\n");
      hqlBuilder.append("ORDER BY m.timestamp DESC\n");
      var query = entityManager
          .createQuery(hqlBuilder.toString(), Measurement.class)
          .setParameter("ownerId", ownerId)
          .setParameter("beehiveId", beehiveId)
          .setParameter("minTimestamp", minTimestamp);
      if (haveType) {
        query.setParameter("type", type);
      }
      return query.getResultList();
    } catch (PersistenceException e) {
      log.error("DataAccessException, ", e);
      throw new DataAccessException(e.toString());
    }
  }
}
