package com.daham.messaging;

public interface RpcServer extends AutoCloseable {
  <T, R> void registerMethod(String method, Class<T> tType, RpcRequestHandler<T, R> handler) throws Exception;
  void start() throws Exception;
}
