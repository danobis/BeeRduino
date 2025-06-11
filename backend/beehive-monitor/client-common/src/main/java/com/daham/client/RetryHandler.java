package com.daham.client;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface RetryHandler<T> {
  CompletableFuture<Boolean> executeAsync(T payload, int attempt) throws Exception;
}
