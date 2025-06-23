package com.daham.services;

import com.daham.database.MeasurementDao;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDateTime;

@Slf4j
@ApplicationScoped
public class RepublishScheduler {
  @Inject
  DataCollectorService service;

  @Inject
  MeasurementDao measurementDao;

  @ConfigProperty(name = "republish.scheduler.enabled", defaultValue = "true")
  boolean enabled;

  @ConfigProperty(name = "republish.scheduler.batch-size", defaultValue = "50")
  int batchSize;

  @ConfigProperty(name = "republish.scheduler.retention-days", defaultValue = "5")
  int retentionDays;

  @Scheduled(every = "15m")
  void republish() {
    if (enabled && !measurementDao.isEmpty()) {
      var measurements = measurementDao.findAll(batchSize);
      service.publishMeasurements(measurements);
      // remove all outdated measurements
      var maxTimestamp = LocalDateTime
          .now()
          .minusDays(retentionDays);
      measurementDao.removeAll(maxTimestamp);
    }
  }
}
