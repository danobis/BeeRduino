package com.daham.collector.rest.resource;

import com.daham.collector.rest.json.MeasurementJson;
import com.daham.messaging.Publisher;
import com.daham.rabbitmq.RabbitmqConnectionFactory;
import com.daham.rabbitmq.RabbitmqPublisher;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@Slf4j
@RequestScoped
@Path("/measurements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MeasurementResource {
  private final Publisher<MeasurementJson> publisher;

  @ConfigProperty(name = "quarkus.application.name")
  String name;

  @ConfigProperty(name = "quarkus.http.port")
  Integer port;

  @Inject
  public MeasurementResource(RabbitmqConnectionFactory connectionFactory) {
    publisher = new RabbitmqPublisher<>(connectionFactory);
  }

  @POST
  public Response createMeasurement(@Valid MeasurementJson inputJson) {
    try {
      publisher.publish(inputJson);
    } catch (Exception e) {
      return Response
          .status(Response.Status.BAD_REQUEST)
          .entity(e.getMessage())
          .build();
    }
    if (port == null) {
      port = ConfigProvider.getConfig()
          .getValue("quarkus.http.port", Integer.class);
    }
    return Response
        .ok("Successfully published message via REST<%s:%d>".formatted(name, port))
        .build();
  }

  @POST
  @Path("/batch")
  public Response createMeasurements(@Valid List<MeasurementJson> inputJson) {
    try {
      for (var measurement : inputJson) {
        publisher.publish(measurement);
      }
    } catch (Exception e) {
      return Response
          .status(Response.Status.BAD_REQUEST)
          .entity(e.getMessage())
          .build();
    }
    if (port == null) {
      port = ConfigProvider.getConfig()
          .getValue("quarkus.http.port", Integer.class);
    }
    return Response
        .ok("Successfully published <%d> messages via REST<%s:%d>".formatted(inputJson.size(), name, port))
        .build();
  }
}
