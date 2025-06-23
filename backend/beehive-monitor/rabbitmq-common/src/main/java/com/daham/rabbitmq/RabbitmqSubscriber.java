package com.daham.rabbitmq;

import com.daham.messaging.MessageHandler;
import com.daham.messaging.MessageSerializer;
import com.daham.messaging.Subscriber;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RabbitmqSubscriber<T> implements Subscriber<T> {
  private static final String DEFAULT_EXCHANGE_NAME = "publish_subscribe_exchange";

  private final String exchangeName;
  private final String queueName;
  private final Connection connection;
  private final Channel channel;

  private final List<MessageHandler<T>> messageHandlers;
  private final Class<T> messageType;

  public RabbitmqSubscriber(RabbitmqConnectionFactory connectionFactory, String exchangeName, Class<T> type) {
    try {
      this.exchangeName = exchangeName;

      connection = connectionFactory.createConnection();
      channel = connection.createChannel();
      channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, true);
      queueName = channel.queueDeclare("", true, false, false, null).getQueue(); // auto generated queue
      channel.queueBind(queueName, exchangeName, "");

      messageHandlers = new ArrayList<>();
      messageType = type;
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  public RabbitmqSubscriber(RabbitmqConnectionFactory connectionFactory, Class<T> type) {
    this(connectionFactory, DEFAULT_EXCHANGE_NAME, type);
  }

  @Override
  public void subscribe(MessageHandler<T> handler) {
    messageHandlers.add(handler);
  }

  @Override
  public void start() throws Exception {
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      try {
        var bytes = delivery.getBody();
        var outputMessage = MessageSerializer.deserialize(bytes, messageType);
        for (var messageHandler : messageHandlers) {
          messageHandler.handle(outputMessage.getPayload()); // notify all
        }
        log.debug("Successfully read message from RabbitMQ<EXCHANGE_NAME={}>", exchangeName);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
    channel.basicConsume(queueName, true, deliverCallback, channel::basicCancel);
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
}
