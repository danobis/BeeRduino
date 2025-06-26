package com.daham.producer.services;

import com.daham.producer.services.json.MeasurementOutputJson;
import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Dependent
@Path("/gateway/collector")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "data-collector-service")
public interface DataCollectorClient {
  @POST
  Response publishMeasurement(MeasurementOutputJson measurement);

  @POST
  @Path("/batch")
  Response publishMeasurements(List<MeasurementOutputJson> measurements);
}
