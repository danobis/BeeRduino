package com.daham.graphql.resources;

import com.daham.domain.Owner;
import com.daham.graphql.json.OwnerInputJson;
import com.daham.graphql.json.OwnerOutputJson;
import com.daham.services.BeehiveService;
import com.daham.services.OwnerService;
import com.daham.utils.ObjectMapperUtils;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import java.util.UUID;

@Slf4j
@GraphQLApi
public class OwnerGraphQLResource {
  @Inject
  OwnerService ownerService;

  @Inject
  BeehiveService beehiveService;

  @Query("get_owner")
  public OwnerOutputJson getOwner(
      @Name("owner_uuid") UUID ownerId,
      @Name("include_beehives") boolean includeBeehives) {
    var owner = ownerService.getOwnerById(ownerId);
    owner.setBeehives(null);
    if (includeBeehives) {
      var beehives = beehiveService.getAllBeehivesByOwner(ownerId);
      log.info("Successfully queried <{}> Beehives for Owner<UUID={}>", beehives.size(), ownerId);
      owner.setBeehives(beehives);
    }
    log.info("Successfully queried Owner<UUID={}>", ownerId);
    return ObjectMapperUtils.map(owner, OwnerOutputJson.class);
  }

  @Mutation("create_owner")
  public OwnerOutputJson createOwner(
      @Name("input_json") @Valid OwnerInputJson inputJson) {
    var owner = ObjectMapperUtils.map(inputJson, Owner.class);
    owner = ownerService.createOwner(owner);
    log.info("Successfully persisted Owner<UUID={}>", owner.getId());
    return ObjectMapperUtils.map(owner, OwnerOutputJson.class);
  }
}
