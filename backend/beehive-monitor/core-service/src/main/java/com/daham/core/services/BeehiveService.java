package com.daham.core.services;

import com.daham.core.domain.Beehive;

import java.util.List;
import java.util.UUID;

public interface BeehiveService {
  List<Beehive> getAllBeehivesByOwner(UUID ownerId) throws BadQueryException;
  Beehive getBeehiveById(UUID ownerId, UUID beehiveId) throws BadQueryException, NotFoundException;
  Beehive createBeehive(Beehive beehive) throws BadQueryException;
}
