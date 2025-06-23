package com.daham.rabbitmq;

import com.daham.messaging.Message;
import com.daham.messaging.MessageSerializer;
import com.daham.messaging.Publisher;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RabbitmqPublisher<T> implements Publisher<T> {
  private static final String DEFAULT_EXCHANGE_NAME = "publish_subscribe_exchange";

  private final String exchangeName;
  private final Connection connection;
  private final Channel channel;

  public RabbitmqPublisher(RabbitmqConnectionFactory connectionFactory, String exchangeName) {
    try {
      this.exchangeName = exchangeName;

      connection = connectionFactory.createConnection();
      channel = connection.createChannel();
      channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, true);
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  public RabbitmqPublisher(RabbitmqConnectionFactory connectionFactory) {
    this(connectionFactory, DEFAULT_EXCHANGE_NAME);
  }

  @Override
  public void publish(T message) throws Exception {
    var inputMessage = Message.create(message);
    channel.basicPublish(exchangeName, "", null, MessageSerializer.serialize(inputMessage));
    log.debug("Successfully published message to RabbitMQ<EXCHANGE_NAME={}>", exchangeName);
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
