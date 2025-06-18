package com.daham.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

@Slf4j
@Readiness
@Liveness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {
  private static final String HEALTH_CHECK_NAME = "BeehiveDatabaseHealthCheck";

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  UserTransaction transaction;

  @Override
  public HealthCheckResponse call() {
    try {
      transaction.setTransactionTimeout(3);
      transaction.begin();
      entityManager.createQuery("SELECT 1").getSingleResult();
      transaction.commit();
      return HealthCheckResponse.up(HEALTH_CHECK_NAME);
    }
    catch (Exception e) {
      log.error("Database health check failed, ERROR: {}", e.toString());
      return HealthCheckResponse
          .named(HEALTH_CHECK_NAME)
          .withData("exception", e.getMessage())
          .down()
          .build();
    }
  }
}
