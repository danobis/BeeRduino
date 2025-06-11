package com.daham.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.collection.spi.PersistentCollection;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Collection;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectMapperUtils {
  private static final ModelMapper modelMapper;

  static {
    modelMapper = new ModelMapper();
    modelMapper.getConfiguration()
        .setMatchingStrategy(MatchingStrategies.STRICT)
        .setPropertyCondition(mappingContext -> {
          var source = mappingContext.getSource();
          if (source instanceof PersistentCollection<?>) {
            return ((PersistentCollection<?>) source).wasInitialized();
          }
          return source != null;
        });
  }

  public static <D, T> D map(final T entity, Class<D> outClass) {
    return modelMapper.map(entity, outClass);
  }

  public static <D, T> List<D> mapAll(final Collection<T> entityList, Class<D> outClass) {
    return entityList.stream()
        .map(entity -> map(entity, outClass))
        .toList();
  }

  public static <S, D> D map(final S source, D destination) {
    modelMapper.map(source, destination);
    return destination;
  }
}
