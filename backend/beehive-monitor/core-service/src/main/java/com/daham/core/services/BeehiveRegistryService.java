package com.daham.core.services;

import com.daham.core.domain.Beehive;

@SuppressWarnings("all")
public interface BeehiveRegistryService extends AutoCloseable {
  void registerBeehive(Beehive beehive);
  void unregisterBeehive(Beehive beehive);
}
