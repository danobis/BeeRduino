package com.daham.core.database;

import com.daham.core.domain.Beehive;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeehiveDao extends Dao<Beehive, UUID> {
  List<Beehive> findAllByOwnerId(UUID ownerId) throws DataAccessException;
  Optional<Beehive> findByOwnerIdAndBeehiveId(UUID ownerId, UUID beehiveId) throws DataAccessException;
}
