package com.daham.services;

import com.daham.domain.Beehive;
import com.daham.rpc.model.Request;
import com.daham.rpc.model.Response;

@SuppressWarnings("all")
public interface BeehiveRegistryService extends AutoCloseable {
  void registerBeehive(Beehive beehive);
  void unregisterBeehive(Beehive beehive);
}
