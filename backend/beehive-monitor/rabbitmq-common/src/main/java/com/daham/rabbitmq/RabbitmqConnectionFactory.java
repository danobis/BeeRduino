package com.daham.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class RabbitmqConnectionFactory {
  private static final boolean DEFAULT_AUTOMATIC_RECOVERY = true;
  private static final long DEFAULT_NETWORK_RECOVERY_INTERVAL = 10_000L;

  private final ConnectionFactory connectionFactory;

  @ConfigProperty(name = "rabbitmq.host", defaultValue = "localhost")
  String rabbitMQHost;

  @ConfigProperty(name = "rabbitmq.port", defaultValue = "5672")
  int rabbitMQPort;

  @ConfigProperty(name = "rabbitmq.username", defaultValue = "guest")
  String rabbitMQUsername;

  @ConfigProperty(name = "rabbitmq.password", defaultValue = "guest")
  String rabbitMQPassword;

  public RabbitmqConnectionFactory() {
    connectionFactory = new ConnectionFactory();
  }

  public Connection createConnection(boolean automaticRecovery, long networkRecoveryInterval) throws IOException, TimeoutException {
    connectionFactory.setHost(rabbitMQHost);
    connectionFactory.setPort(rabbitMQPort);
    connectionFactory.setUsername(rabbitMQUsername);
    connectionFactory.setPassword(rabbitMQPassword);
    connectionFactory.setAutomaticRecoveryEnabled(automaticRecovery);
    connectionFactory.setNetworkRecoveryInterval(networkRecoveryInterval);
    return connectionFactory.newConnection();
  }

  public Connection createConnection() throws IOException, TimeoutException {
    return createConnection(DEFAULT_AUTOMATIC_RECOVERY, DEFAULT_NETWORK_RECOVERY_INTERVAL);
  }
}
