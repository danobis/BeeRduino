package com.daham.core.services;

import com.daham.core.database.BeehiveDao;
import com.daham.core.database.DataAccessException;
import com.daham.core.database.MeasurementDao;
import com.daham.core.database.OwnerDao;
import com.daham.core.domain.Measurement;
import com.daham.core.domain.SensorType;
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
  OwnerDao ownerDao;

  @Inject
  BeehiveDao beehiveDao;

  @Inject
  EventPublisher eventPublisher;

  @Override
  public List<Measurement> getAllMeasurementsByBeehive(UUID ownerId, UUID beehiveId, SensorType type, HistorySpan span) throws BadQueryException, NotFoundException {
    try {
      if (!ownerDao.exists(ownerId)) {
        throw new BadQueryException("Owner<UUID='%s'> does not exist".formatted(ownerId));
      }
      var result = beehiveDao.findByOwnerIdAndBeehiveId(ownerId, beehiveId);
      if (result.isEmpty()) {
        throw new NotFoundException("Beehive<UUID='%s'> not found for Owner<UUID='%s'>".formatted(beehiveId, ownerId));
      }
      return measurementDao.findAllByOwnerIdAndBeehiveId(ownerId, beehiveId, type, span);
    } catch (DataAccessException e) {
      throw new InternalServerException(e.toString());
    }
  }

  @Override
  public Measurement createMeasurement(Measurement measurement) throws BadQueryException, NotFoundException {
    try {
      final var ownerId = measurement.getOwnerId();
      final var beehiveId = measurement.getBeehiveId();
      if (!ownerDao.exists(ownerId)) {
        throw new BadQueryException("Owner<UUID='%s'> does not exist".formatted(ownerId));
      }
      var result = beehiveDao.findByOwnerIdAndBeehiveId(ownerId, beehiveId);
      if (result.isEmpty()) {
        throw new NotFoundException("Beehive<UUID='%s'> not found for Owner<UUID='%s'>".formatted(beehiveId, ownerId));
      }
      measurement = measurementDao.merge(measurement);
      var between = Duration.between(measurement.getTimestamp(),  LocalDateTime.now());
      if (between.getSeconds() <= 15L) {
        eventPublisher.publish(measurement);
      }
      return measurement;
    } catch (DataAccessException e) {
      throw new InternalServerException(e.toString());
    }
  }
}
