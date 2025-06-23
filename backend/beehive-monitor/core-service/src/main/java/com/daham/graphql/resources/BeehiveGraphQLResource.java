package com.daham.graphql.resources;

import com.daham.domain.Beehive;
import com.daham.graphql.json.BeehiveInputJson;
import com.daham.graphql.json.BeehiveOutputJson;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@GraphQLApi
public class BeehiveGraphQLResource {
  @Inject
  BeehiveService beehiveService;

  @Inject
  OwnerService ownerService;

  @Query("get_beehives")
  public List<BeehiveOutputJson> getBeehives(
      @Name("owner_uuid") UUID ownerId) {
    var beehives = beehiveService.getAllBeehivesByOwner(ownerId);
    log.info("Successfully queried <{}> Beehives for Owner<UUID={}>", beehives.size(), ownerId);
    return ObjectMapperUtils.mapAll(beehives, BeehiveOutputJson.class);
  }

  @Query("get_beehive")
  public BeehiveOutputJson getBeehive(
      @Name("owner_uuid") UUID ownerId,
      @Name("beehive_uuid") UUID beehiveId) {
    var beehive = beehiveService.getBeehiveById(ownerId, beehiveId);
    var owner = ownerService.getOwnerById(ownerId);
    owner.setBeehives(null);
    var outputJson = ObjectMapperUtils.map(beehive, BeehiveOutputJson.class);
    outputJson.setOwner(ObjectMapperUtils.map(owner, OwnerOutputJson.class));
    log.info("Successfully retrieved Beehive<UUID={}> for Owner<UUID={}>", beehiveId, ownerId);
    return outputJson;
  }

  @Mutation("create_beehive")
  public BeehiveOutputJson createBeehive(
      @Name("input_json") @Valid BeehiveInputJson inputJson) {
    var beehive = ObjectMapperUtils.map(inputJson, Beehive.class);
    beehive = beehiveService.createBeehive(beehive);
    log.info("Successfully created Beehive<UUID={}> for Owner<UUID={}>", beehive.getId(), beehive.getOwnerId());
    return ObjectMapperUtils.map(beehive, BeehiveOutputJson.class);
  }
}
