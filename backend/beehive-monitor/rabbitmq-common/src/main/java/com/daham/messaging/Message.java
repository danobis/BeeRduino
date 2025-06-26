package com.daham.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message<T> {
  private T payload;

  public boolean isSuccess() {
    return payload != null && !(payload instanceof Throwable);
  }

  public Throwable getCause() {
    if (payload instanceof Throwable) {
      return (Throwable) payload;
    }
    return null;
  }

  public static <T> Message<T> create(T payload) {
    return new Message<>(payload);
  }
}
