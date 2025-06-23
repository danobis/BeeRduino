package com.daham.graphql.resources;

import com.daham.domain.SensorType;
import com.daham.graphql.json.MeasurementOutputJson;
import com.daham.services.EventPublisher;
import com.daham.services.HistorySpan;
import com.daham.services.MeasurementService;
import com.daham.utils.ObjectMapperUtils;
import io.smallrye.graphql.api.Subscription;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.UUID;

@Slf4j
@GraphQLApi
@SuppressWarnings("all")
public class MeasurementGraphQLResource {
  @Inject
  MeasurementService measurementService;

  @Inject
  EventPublisher eventPublisher;

  @Query("get_measurements")
  public List<MeasurementOutputJson> getMeasurements(
      @Name("beehive_uuid") UUID beehiveId,
      @Name("sensor_type") SensorType type,
      @Name("history_span") HistorySpan span) {
    var measurements = measurementService.getAllMeasurementsByBeehive(beehiveId, type, span);
    log.info("Successfully queried Measurements for Beehive<UUID={}>", beehiveId);
    return ObjectMapperUtils.mapAll(measurements, MeasurementOutputJson.class);
  }

  @Subscription("on_measurement")
  public Publisher<MeasurementOutputJson> onMeasurement(
      @Name("beehive_uuid") UUID beehiveId,
      @Name("sensor_type") SensorType type) {
    log.info("Subscribing to Measurements from Beehive<UUID={}>", beehiveId);
    return ReactiveStreams
        .fromPublisher(eventPublisher.getPublisher())
        .filter(m -> m.getBeehiveId().equals(beehiveId) && m.getType() == type)
        .map(m -> ObjectMapperUtils.map(m, MeasurementOutputJson.class))
        .buildRs();
  }
}
