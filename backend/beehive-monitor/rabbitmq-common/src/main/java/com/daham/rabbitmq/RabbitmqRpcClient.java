package com.daham.rabbitmq;

import com.daham.messaging.Message;
import com.daham.messaging.MessageSerializer;
import com.daham.messaging.RpcClient;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RabbitmqRpcClient implements RpcClient {
  private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(60);

  private final Connection connection;
  private final Channel channel;

  public RabbitmqRpcClient(RabbitmqConnectionFactory connectionFactory) {
    try {
      connection = connectionFactory.createConnection();
      channel = connection.createChannel();
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T, R> R execute(String method, T request, Class<R> rType, Duration timeout) throws Exception {
    String responseConsumerTag = null;
    try {
      final String requestQueueName = "rpc_%s_request_queue".formatted(method);
      final String responseQueueName = "rpc_%s_response_queue".formatted(method);
      channel.queueDeclare(requestQueueName, false, false, false, null);
      channel.queueDeclare(responseQueueName, false, false, false, null);
      // create random requestId
      final String requestId = getRandomRequestId();
      var inputProperties = new BasicProperties.Builder()
          .correlationId(requestId)
          .replyTo(responseQueueName)
          .build();
      var inputMessage = Message.create(request);
      channel.basicPublish("", requestQueueName, inputProperties, MessageSerializer.serialize(inputMessage));

      CompletableFuture<Message<?>> responseFuture = new CompletableFuture<>();
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        var outputProperties = delivery.getProperties();
        if (outputProperties.getCorrelationId().equals(requestId)) {
          var bytes = delivery.getBody();
          try {
            var outputMessage = MessageSerializer.deserialize(bytes, rType);
            responseFuture.complete(outputMessage);
          } catch (Exception e) {
            var outputMessage = MessageSerializer.deserialize(bytes, Throwable.class);
            responseFuture.complete(outputMessage);
          }
        }
      };
      responseConsumerTag = channel.basicConsume(responseQueueName, true, deliverCallback, channel::basicCancel);

      var outputMessage = responseFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
      if (!outputMessage.isSuccess()) {
        var throwable = outputMessage.getCause();
        log.error("Unable to execute RPC<Method='{}'>, ERROR", method, throwable);
        throw new RuntimeException(throwable);
      }
      return (R) outputMessage.getPayload();
      } finally {
        if (responseConsumerTag != null) {
          channel.basicCancel(responseConsumerTag);
        }
    }
  }

  @Override
  public <T, R> R execute(String method, T request, Class<R> rType) throws Exception {
    return execute(method, request, rType, DEFAULT_REQUEST_TIMEOUT);
  }

  @Override
  public void close() throws Exception {
    if (channel != null && channel.isOpen()) {
      channel.close();
    }
    if (connection != null && connection.isOpen()) {
      connection.close();
    }
  }

  private String getRandomRequestId() {
    var random = ThreadLocalRandom.current();
    var prefix = System.currentTimeMillis();
    var suffix = random.nextInt(0, 1_000_000);
    return "%d%06d".formatted(prefix, suffix);
  }
}
