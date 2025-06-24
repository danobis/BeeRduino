package com.daham.services;

import com.daham.database.DataAccessException;
import com.daham.database.OwnerDao;
import com.daham.domain.Owner;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Dependent
@Transactional
public class OwnerServiceBean implements OwnerService {
  @Inject
  OwnerDao ownerDao;

  @Override
  public Owner getOwnerById(UUID id) throws NotFoundException {
    try {
      var result = ownerDao.findById(id);
      if (result.isEmpty()) {
        throw new NotFoundException("Owner<UUID='%s'> not found".formatted(id));
      }
      return result.get();
    } catch (DataAccessException e) {
      throw new InternalServerException(e.getMessage());
    }
  }

  @Override
  public Owner getOwnerByEmail(String email) throws NotFoundException {
    try {
      var result = ownerDao.findOwnerByEmail(email);
      if (result.isEmpty()) {
        throw new NotFoundException("Owner<E-Mail='%s'> not found".formatted(email));
      }
      return result.get();
    } catch (DataAccessException e) {
      throw new InternalServerException(e.getMessage());
    }
  }

  @Override
  public Owner createOwner(Owner owner) {
    try {
      if (owner.getTimestamp() == null) {
        owner.setTimestamp(LocalDateTime.now());
      }
      return ownerDao.merge(owner);
    } catch (DataAccessException e) {
      throw new InternalServerException(e.getMessage());
    }
  }
}
