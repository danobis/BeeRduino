package com.daham.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Collection;
import java.util.List;

/**
 * Utility class for object mapping using ModelMapper.
 * This class provides methods for mapping objects between different types.
 * <p>
 * Initializes the ModelMapper instance with default property settings.
 * Default property matching strategy is set to Strict.
 * Custom mappings can be added using {@link ModelMapper#addMappings(PropertyMap)}.
 * </p>
 *
 * @author Daniel Hametner
 * @version 1.2
 */
@SuppressWarnings("all")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectMapperUtils {
  private static final ModelMapper modelMapper;

  static {
    modelMapper = new ModelMapper();
    modelMapper.getConfiguration()
        .setMatchingStrategy(MatchingStrategies.STRICT);
  }

  /**
   * Maps an entity to an object of the specified class.
   *
   * @param <D>      The type of the result object.
   * @param <T>      The type of the source object to map from.
   * @param entity   The entity that needs to be mapped.
   * @param outClass The class of the result object.
   * @return A new object of the specified class.
   */
  public static <D, T> D map(final T entity, Class<D> outClass) {
    return modelMapper.map(entity, outClass);
  }

  /**
   * Maps a source object to a destination object.
   *
   * @param <S>         The type of the source object.
   * @param <D>         The type of the destination object.
   * @param source      The object to map from.
   * @param destination The object to map to.
   * @return The mapped destination object.
   */
  public static <S, D> D map(final S source, D destination) {
    modelMapper.map(source, destination);
    return destination;
  }

  /**
   * Maps a list of entities to a list of objects of the specified class.
   *
   * @param <D>        The type of objects in the result list.
   * @param <T>        The type of entity in the entity list.
   * @param entityList The list of entities that needs to be mapped.
   * @param outClass   The class of the result list element.
   * @return A list of mapped objects with the specified type.
   */
  public static <D, T> List<D> mapAll(final Collection<T> entityList, Class<D> outClass) {
    return entityList.stream()
        .map(entity -> map(entity, outClass))
        .toList();
  }
}
