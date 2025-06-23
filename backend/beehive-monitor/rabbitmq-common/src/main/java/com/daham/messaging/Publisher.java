package com.daham.messaging;

public interface Publisher<T> extends AutoCloseable {
  void publish(T message) throws Exception;
}
