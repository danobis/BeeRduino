package com.daham.messaging;

@FunctionalInterface
public interface MessageHandler<T> {
  void handle(T message) throws Exception;
}
