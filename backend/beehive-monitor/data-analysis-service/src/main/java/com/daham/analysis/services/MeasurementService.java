package com.daham.analysis.services;

import com.daham.analysis.domain.Measurement;
import com.daham.analysis.domain.SensorType;

import java.util.List;
import java.util.UUID;

public interface MeasurementService {
  List<Measurement> getAllMeasurementsByBeehive(UUID beehiveId, SensorType type, HistorySpan span) throws BadQueryException, NotFoundException;
  Measurement createMeasurement(Measurement measurement) throws BadQueryException, NotFoundException;
}
