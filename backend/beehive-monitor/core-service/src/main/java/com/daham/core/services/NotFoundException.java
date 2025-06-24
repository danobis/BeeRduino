package com.daham.core.services;

import io.smallrye.graphql.api.ErrorCode;

@ErrorCode("NOT_FOUND_EXCEPTION")
public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }
}
