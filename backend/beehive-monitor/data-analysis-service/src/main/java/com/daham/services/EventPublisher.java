package com.daham.services;

import com.daham.domain.Measurement;
import jakarta.enterprise.context.ApplicationScoped;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

import java.util.concurrent.SubmissionPublisher;

@ApplicationScoped
public class EventPublisher implements AutoCloseable  {
  private final SubmissionPublisher<Measurement> publisher;

  public EventPublisher() {
    this.publisher = new SubmissionPublisher<>();
  }

  public Publisher<Measurement> getPublisher() {
    return FlowAdapters.toPublisher(publisher);
  }

  public void publish(Measurement measurement) {
    publisher.submit(measurement);
  }

  @Override
  public void close() {
    publisher.close();
  }
}
