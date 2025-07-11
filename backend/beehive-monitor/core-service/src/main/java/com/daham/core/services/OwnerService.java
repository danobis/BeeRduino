package com.daham.core.services;

import com.daham.core.domain.Owner;

import java.util.UUID;

public interface OwnerService {
  Owner getOwnerById(UUID id) throws NotFoundException;
  Owner getOwnerByEmail(String email) throws NotFoundException;
  Owner createOwner(Owner owner);
}
