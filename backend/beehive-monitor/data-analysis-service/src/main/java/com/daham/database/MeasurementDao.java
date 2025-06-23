package com.daham.database;

import com.daham.domain.Measurement;
import com.daham.domain.SensorType;
import com.daham.services.HistorySpan;

import java.util.List;
import java.util.UUID;

public interface MeasurementDao extends Dao<Measurement, UUID> {
  List<Measurement> findAllByBeehiveId(UUID beehiveId, SensorType type, HistorySpan span) throws DataAccessException;
}
