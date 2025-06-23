package com.daham.rabbitmq;

import com.daham.messaging.Message;
import com.daham.messaging.MessageSerializer;
import com.daham.messaging.RpcRequestHandler;
import com.daham.messaging.RpcServer;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RabbitmqRpcServer implements RpcServer {
  private final Connection connection;
  private final Channel channel;

  private final Map<String, RabbitmqMethodEntry> requestMethods;
  private final Map<String, String> requestConsumerTags;
  private volatile boolean isRunning = false;

  public RabbitmqRpcServer(RabbitmqConnectionFactory connectionFactory) {
    try {
      connection = connectionFactory.createConnection();
      channel = connection.createChannel();

      requestMethods = new HashMap<>();
      requestConsumerTags = new HashMap<>();
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T, R> void registerMethod(String method, Class<T> tType, RpcRequestHandler<T, R> handler) throws Exception {
    final String requestQueueName = "rpc_%s_request_queue".formatted(method);
    channel.queueDeclare(requestQueueName, false, false, false, null);
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      var envelope = delivery.getEnvelope();
      var inputProperties = delivery.getProperties();
      var outputProperties = new BasicProperties.Builder()
          .correlationId(inputProperties.getCorrelationId())
          .build();
      Message<?> outputMessage = null;
      try {
        var bytes = delivery.getBody();
        var inputMessage = MessageSerializer.deserialize(bytes, tType);
        T request = inputMessage.getPayload();
        R response = handler.handle(request);
        outputMessage = Message.create(response);
      } catch (Exception e) {
        log.error("Unable to process RPC<Method='{}'> request, ERROR", method, e);
        outputMessage = Message.create(e);
      } finally {
        var responseQueueName = inputProperties.getReplyTo();
        if (outputMessage != null && responseQueueName != null) {
          channel.basicPublish("", responseQueueName, outputProperties, MessageSerializer.serialize(outputMessage));
        }
        channel.basicAck(envelope.getDeliveryTag(), false);
      }
    };
    requestMethods.put(method, new RabbitmqMethodEntry(deliverCallback, channel::basicCancel));
  }

  @Override
  public void start() throws Exception {
    if (!isRunning) {
      for (var method : requestMethods.keySet()) {
        final String requestQueueName = "rpc_%s_request_queue".formatted(method);
        var entry = requestMethods.get(method);
        var deliverCallback = entry.deliverCallback;
        var cancelCallback = entry.cancelCallback;
        var consumerTag = channel.basicConsume(requestQueueName, false, deliverCallback, cancelCallback);
        requestConsumerTags.put(method, consumerTag);
      }
      isRunning = true;
    } else {
      log.warn("RPC Server is already running");
    }
  }

  @Override
  public void close() throws Exception {
    if (isRunning) {
      for (var entry : requestConsumerTags.entrySet()) {
        channel.basicCancel(entry.getValue());
        log.debug("Closing consumer for RPC<Method='{}'>", entry.getKey());
      }
      requestConsumerTags.clear();
      if (channel != null && channel.isOpen()) {
        channel.close();
      }
      if (connection != null && connection.isOpen()) {
        connection.close();
      }
      isRunning = false;
    } else {
      log.warn("RPC Server is already closed");
    }
  }

  @AllArgsConstructor
  private static class RabbitmqMethodEntry {
    DeliverCallback deliverCallback;
    CancelCallback cancelCallback;
  }
}
