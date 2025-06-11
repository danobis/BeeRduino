package com.daham.client.utils;

import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyNamingStrategy;
import jakarta.json.bind.config.PropertyOrderStrategy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtils {
  private static final JsonbConfig JSONB_CONFIG;

  static {
    JSONB_CONFIG = new JsonbConfig()
        .withFormatting(true)
        .withNullValues(false)
        .withPropertyOrderStrategy(PropertyOrderStrategy.LEXICOGRAPHICAL)
        .withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES)
        .withDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
  }

  public static <T> String toJsonString(T object) {
    try(var jsonb = JsonbBuilder.create(JSONB_CONFIG)) {
      return jsonb.toJson(object);
    } catch (Exception e) {
      log.error("Unable to serialize object<{}> to JSON, ERROR: {}", object.getClass(), e.toString());
      throw new RuntimeException(e);
    }
  }

  public static <T> T fromJsonString(String object, Class<T> type) {
    try(var jsonb = JsonbBuilder.create(JSONB_CONFIG)) {
      return jsonb.fromJson(object, type);
    } catch (Exception e) {
     log.error("Unable to deserialize object<{}> from JSON, ERROR: {}", object, e.toString());
     throw new RuntimeException(e);
    }
  }
}
