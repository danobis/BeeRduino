package com.daham.producer.services;

import com.daham.common.utils.ObjectMapperUtils;
import com.daham.producer.database.DataAccessException;
import com.daham.producer.database.MeasurementDao;
import com.daham.producer.domain.Measurement;
import com.daham.producer.services.json.MeasurementOutputJson;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@Slf4j
@Transactional
@ApplicationScoped
@SuppressWarnings("all")
public class DataCollectorService {
  @Inject
  @RestClient
  DataCollectorClient client;

  @Inject
  MeasurementDao measurementDao;

  @Retry(maxRetries = 5, delay = 500L)
  @Fallback(fallbackMethod = "storeMeasurement")
  public void publishMeasurement(Measurement measurement) {
    var outputJson = ObjectMapperUtils.map(measurement, MeasurementOutputJson.class);
    try (var response = client.publishMeasurement(outputJson)) {
      log.debug("REST single publish response is: {}", response.readEntity(String.class));
    } catch (Exception e) {
      log.error("Unable to publish measurement, ERROR", e);
      throw e; // rethrow to trigger retry
    }
  }

  @Retry(maxRetries = 5, delay = 2000L)
  public void publishMeasurements(List<Measurement> measurements) {
    var outputJson = ObjectMapperUtils.mapAll(measurements, MeasurementOutputJson.class);
    try (var response = client.publishMeasurements(outputJson)) {
      log.debug("REST batch publish response is: {}", response.readEntity(String.class));
    } catch (Exception e) {
      log.error("Unable to publish measurements, ERROR", e);
      throw e; // rethrow to trigger retry
    }
  }

  private void storeMeasurement(Measurement measurement) {
    try {
      log.warn("All retry attempts exhausted for Measurement<Timestamp={}>", measurement.getTimestamp());
      log.warn("Storing measurement to H2 fallback storage");
      measurementDao.persist(measurement);
    } catch (DataAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
