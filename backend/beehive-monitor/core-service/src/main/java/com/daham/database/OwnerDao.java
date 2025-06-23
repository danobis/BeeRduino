package com.daham.database;

import com.daham.domain.Owner;

import java.util.Optional;
import java.util.UUID;

public interface OwnerDao extends Dao<Owner, UUID> {
  Optional<Owner> findOwnerByEmail(String email) throws DataAccessException;
}
