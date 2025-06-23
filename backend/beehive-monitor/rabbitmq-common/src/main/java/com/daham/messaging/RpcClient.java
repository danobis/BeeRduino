package com.daham.messaging;

import java.time.Duration;

public interface RpcClient extends AutoCloseable {
  <T, R> R execute(String method, T request, Class<R> rType, Duration timeout) throws Exception;
  <T, R> R execute(String method, T request, Class<R> rType) throws Exception;
}
