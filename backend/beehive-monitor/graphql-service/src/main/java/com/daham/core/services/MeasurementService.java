package com.daham.core.services;

import com.daham.core.domain.Measurement;
import com.daham.core.domain.SensorType;

import java.util.List;
import java.util.UUID;

public interface MeasurementService {
  List<Measurement> getAllMeasurementsByBeehive(UUID ownerId, UUID beehiveId, SensorType type, HistorySpan span) throws BadQueryException, NotFoundException;
  Measurement createMeasurement(Measurement measurement) throws BadQueryException, NotFoundException;
}
