package com.daham.services;

import com.daham.database.MeasurementDao;
import com.daham.domain.Measurement;
import com.daham.messaging.Subscriber;
import com.daham.rabbitmq.RabbitmqConnectionFactory;
import com.daham.rabbitmq.RabbitmqSubscriber;
import com.daham.services.json.MeasurementInputJson;
import com.daham.utils.ObjectMapperUtils;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
@ApplicationScoped
public class MeasurementsConsumer implements AutoCloseable {
  private final Subscriber<MeasurementInputJson> subscriber;

  @Inject
  MeasurementService measurementService;

  @Inject
  public MeasurementsConsumer(RabbitmqConnectionFactory connectionFactory) {
    subscriber = new RabbitmqSubscriber<>(connectionFactory, MeasurementInputJson.class);
  }

  void onStart(@Observes StartupEvent event) {
    subscriber.subscribe(message -> {
      var measurement = ObjectMapperUtils.map(message, Measurement.class);
      measurementService.createMeasurement(measurement);
    });
    CompletableFuture.runAsync(() -> {
      try {
        subscriber.start();
      } catch (Exception e) {
        log.error("Unable to start Subscriber, ERROR", e);
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public void close() throws Exception {
    subscriber.close();
  }
}
