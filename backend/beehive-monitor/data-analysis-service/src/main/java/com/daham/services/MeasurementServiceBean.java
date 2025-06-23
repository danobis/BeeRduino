package com.daham.services;

import com.daham.database.BeehiveRegistryDao;
import com.daham.database.DataAccessException;
import com.daham.database.MeasurementDao;
import com.daham.domain.Measurement;
import com.daham.domain.RegistryEntry;
import com.daham.domain.SensorType;
import com.daham.domain.StatusType;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Dependent
@Transactional
public class MeasurementServiceBean implements MeasurementService {
  @Inject
  MeasurementDao measurementDao;

  @Inject
  BeehiveRegistryDao beehiveRegistryDao;

  @Inject
  EventPublisher eventPublisher;

  @Override
  public List<Measurement> getAllMeasurementsByBeehive(UUID beehiveId, SensorType type, HistorySpan span) throws BadQueryException, NotFoundException {
    try {
      return measurementDao.findAllByBeehiveId(beehiveId, type, span);
    } catch (DataAccessException e) {
      throw new InternalServerException(e.toString());
    }
  }

  @Override
  public Measurement createMeasurement(Measurement measurement) throws BadQueryException, NotFoundException {
    try {
      measurement = measurementDao.merge(measurement);
      var between = Duration.between(measurement.getTimestamp(),  LocalDateTime.now());
      if (between.getSeconds() <= 15L) {
        eventPublisher.publish(measurement);
      }
      var beehiveId = measurement.getBeehiveId();
      if (!beehiveRegistryDao.exists(beehiveId)) {
        var entry = new RegistryEntry();
        entry.setBeehiveId(beehiveId);
        entry.setTimestamp(LocalDateTime.now());
        entry.setStatus(StatusType.ORPHANED);
        beehiveRegistryDao.persist(entry);
      }
      return measurement;
    } catch (DataAccessException e) {
      throw new InternalServerException(e.toString());
    }
  }
}
