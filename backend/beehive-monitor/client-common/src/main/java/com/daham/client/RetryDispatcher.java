package com.daham.client;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
public class RetryDispatcher<T> implements AutoCloseable {
  private static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;

  private final int maxRetryAttempts;
  private final RetryHandler<T> retryHandler;
  private final Duration retryDelay;

  private final ScheduledExecutorService executor;
  private final ConcurrentMap<UUID, RetryEntry<T>> queue;

  public RetryDispatcher(int maxRetryAttempts, RetryHandler<T> retryHandler, Duration retryDelay) {
    this.maxRetryAttempts = maxRetryAttempts;
    this.retryHandler = retryHandler;
    this.retryDelay = retryDelay;

    executor = Executors.newScheduledThreadPool(4);
    queue = new ConcurrentHashMap<>(1024);

    executor.scheduleAtFixedRate(this::sendRetry, retryDelay.toSeconds(), retryDelay.toSeconds(), TimeUnit.SECONDS);
  }

  public RetryDispatcher(RetryHandler<T> retryHandler, Duration retryDelay) {
    this(DEFAULT_MAX_RETRY_ATTEMPTS, retryHandler, retryDelay);
  }

  public void sendRetry(T payload) {
    var retryId = UUID.randomUUID();
    log.info("Sending RetryEntry<UUID='{}'>, retrying after {}", retryId, retryDelay);
    queue.put(retryId, new RetryEntry<>(payload, 1));
  }

  private void sendRetry() {
    queue.forEach((id, retryEntry) -> {
      try {
        retryHandler.executeAsync(retryEntry.getPayload(), retryEntry.getAttempt())
            .orTimeout(5, TimeUnit.SECONDS)
            .whenComplete((success, throwable) -> {
              if (throwable != null || !success) {
                retryEntry.incrementAttempt();
                queue.put(id, retryEntry);
              }
              if (success) {
                log.info("Successfully sent RetryEntry<UUID='{}'> after attempt {}", id, retryEntry.getAttempt());
                queue.remove(id);
              }
            });
        if (retryEntry.getAttempt() >= maxRetryAttempts) {
          log.warn("RetryEntry<UUID='{}'> exceeded max retries (MAX_RETRY_ATTEMPTS={})", id, maxRetryAttempts);
          queue.remove(id);
        }
      } catch (Exception e) {
        log.error("Failed to send RetryEntry<UUID='{}'>, ERROR: {}", id, e.toString());
        retryEntry.incrementAttempt();
        queue.put(id, retryEntry);
      }
    });
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }
}
