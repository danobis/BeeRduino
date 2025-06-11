package com.daham.core.database;

import com.daham.core.domain.Owner;

import java.util.Optional;
import java.util.UUID;

public interface OwnerDao extends Dao<Owner, UUID> {
  Optional<Owner> findOwnerByEmail(String email) throws DataAccessException;
}
