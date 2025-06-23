package com.daham.messaging;

public interface Subscriber<T> extends AutoCloseable {
  void subscribe(MessageHandler<T> handler);
  void start() throws Exception;
}
