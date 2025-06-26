package com.daham.messaging;

@FunctionalInterface
public interface RpcRequestHandler<T, R> {
  R handle(T request) throws Exception;
}
