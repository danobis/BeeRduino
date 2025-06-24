package com.daham.services;

import com.daham.database.MeasurementDao;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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

  @Transactional(rollbackOn = Exception.class)
  @Scheduled(every = "15m")
  void republish() {
    if (enabled && !measurementDao.isEmpty(batchSize)) {
      var measurements = measurementDao.findAll(batchSize);
      // remove all outdated and successfully published measurements
      measurementDao.removeAllCached();
      var maxTimestamp = LocalDateTime
          .now()
          .minusDays(retentionDays);
      measurementDao.removeAllOutdated(maxTimestamp);
      service.publishMeasurements(measurements);
    }
  }
}
