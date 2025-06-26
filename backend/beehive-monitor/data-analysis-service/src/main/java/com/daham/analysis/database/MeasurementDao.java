package com.daham.analysis.database;

import com.daham.analysis.domain.Measurement;
import com.daham.analysis.domain.SensorType;
import com.daham.analysis.services.HistorySpan;

import java.util.List;
import java.util.UUID;

public interface MeasurementDao extends Dao<Measurement, UUID> {
  List<Measurement> findAllByBeehiveId(UUID beehiveId, SensorType type, HistorySpan span) throws DataAccessException;
}
