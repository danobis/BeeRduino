package com.daham.services;

import io.smallrye.graphql.api.ErrorCode;

@ErrorCode("INTERNAL_SERVER_EXCEPTION")
public class InternalServerException extends RuntimeException {
  public InternalServerException(String message) {
    super(message);
  }
}
