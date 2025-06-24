package com.daham.core.services;

import io.smallrye.graphql.api.ErrorCode;

@ErrorCode("BAD_QUERY_EXCEPTION")
public class BadQueryException extends RuntimeException {
  public BadQueryException(String message) {
    super(message);
  }
}
