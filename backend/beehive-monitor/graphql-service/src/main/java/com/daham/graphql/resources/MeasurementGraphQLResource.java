package com.daham.graphql.resources;

import com.daham.core.domain.SensorType;
import com.daham.core.services.EventPublisher;
import com.daham.core.services.HistorySpan;
import com.daham.core.services.MeasurementService;
import com.daham.core.utils.ObjectMapperUtils;
import com.daham.graphql.json.MeasurementOutputJson;
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
public class MeasurementGraphQLResource {
  @Inject
  MeasurementService measurementService;

  @Inject
  EventPublisher eventPublisher;

  @Query("get_measurements")
  public List<MeasurementOutputJson> getMeasurements(
      @Name("owner_uuid") UUID ownerId,
      @Name("beehive_uuid") UUID beehiveId,
      @Name("sensor_type") SensorType type,
      @Name("history_span") HistorySpan span) {
    var measurements = measurementService.getAllMeasurementsByBeehive(ownerId, beehiveId, type, span);
    log.info("Successfully queried Measurements for Beehive<UUID='{}'> and Owner<UUID='{}'>", beehiveId, ownerId);
    return ObjectMapperUtils.mapAll(measurements, MeasurementOutputJson.class);
  }

  @Subscription("on_measurement")
  public Publisher<MeasurementOutputJson> onMeasurement(
      @Name("owner_uuid") UUID ownerId,
      @Name("beehive_uuid") UUID beehiveId,
      @Name("sensor_type") SensorType type) {
    log.info("Subscribing to Measurements from Beehive<UUID='{}'> and Owner<UUID='{}'>", beehiveId, ownerId);
    return ReactiveStreams
        .fromPublisher(eventPublisher.getPublisher())
        .filter(m ->
            m.getOwnerId().equals(ownerId)
                && m.getBeehiveId().equals(beehiveId)
                && m.getType() == type)
        .map(m -> ObjectMapperUtils.map(m, MeasurementOutputJson.class))
        .buildRs();
  }
}
