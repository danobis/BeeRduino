package com.daham.services;

import com.daham.database.BeehiveDao;
import com.daham.database.DataAccessException;
import com.daham.database.LocationDao;
import com.daham.database.OwnerDao;
import com.daham.domain.Beehive;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Dependent
@Transactional
public class BeehiveServiceBean implements BeehiveService {
  @Inject
  BeehiveDao beehiveDao;

  @Inject
  LocationDao locationDao;

  @Inject
  OwnerDao ownerDao;

  @Inject
  BeehiveRegistryClient registryClient;

  @Override
  public List<Beehive> getAllBeehivesByOwner(UUID ownerId) throws BadQueryException {
    try {
      if (!ownerDao.exists(ownerId)) {
        throw new BadQueryException("Owner<UUID='%s'> does not exist".formatted(ownerId));
      }
      return beehiveDao.findAllByOwnerId(ownerId);
    } catch (DataAccessException e) {
      throw new InternalServerException(e.getMessage());
    }
  }

  @Override
  public Beehive getBeehiveById(UUID ownerId, UUID beehiveId) throws BadQueryException {
    try {
      if (!ownerDao.exists(ownerId)) {
        throw new BadQueryException("Owner<UUID='%s'> does not exist".formatted(ownerId));
      }
      var result = beehiveDao.findByOwnerIdAndBeehiveId(ownerId, beehiveId);
      if (result.isEmpty()) {
        throw new NotFoundException("Beehive<UUID='%s'> not found for Owner<UUID='%s'>".formatted(beehiveId, ownerId));
      }
      return result.get();
    } catch (DataAccessException e) {
      throw new InternalServerException(e.getMessage());
    }
  }

  @Override
  public Beehive createBeehive(Beehive beehive) throws BadQueryException {
    try {
      final var ownerId = beehive.getOwnerId();
      if (!ownerDao.exists(ownerId)) {
        throw new BadQueryException("Owner<UUID='%s'> does not exist".formatted(ownerId));
      }
      var location = beehive.getLocation();
      if (location == null) {
        var result = locationDao.findById(beehive.getLocationId());
        if (result.isEmpty()) {
          throw new BadQueryException("Location<UUID='%s'> does not exist".formatted(beehive.getLocationId()));
        }
        location = result.get();
      }
      if (beehive.getTimestamp() == null) {
        beehive.setTimestamp(LocalDateTime.now());
      }
      if (location.getTimestamp() == null) {
        location.setTimestamp(beehive.getTimestamp());
      }
      beehive.setLocation(location);
      beehive = beehiveDao.merge(beehive);
      registryClient.registerBeehive(beehive);
      return beehive;
    } catch (DataAccessException e) {
      throw new InternalServerException(e.getMessage());
    }
  }
}
