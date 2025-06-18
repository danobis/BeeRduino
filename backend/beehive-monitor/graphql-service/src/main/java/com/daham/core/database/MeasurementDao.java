package com.daham.core.database;

import com.daham.core.domain.Measurement;
import com.daham.core.domain.SensorType;
import com.daham.core.services.HistorySpan;

import java.util.List;
import java.util.UUID;

public interface MeasurementDao extends Dao<Measurement, UUID> {
  List<Measurement> findAllByOwnerIdAndBeehiveId(UUID ownerId, UUID beehiveId, SensorType type, HistorySpan span) throws DataAccessException;
}
