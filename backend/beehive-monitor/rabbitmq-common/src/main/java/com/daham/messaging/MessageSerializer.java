package com.daham.messaging;

import io.quarkus.runtime.types.ParameterizedTypeImpl;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyNamingStrategy;
import jakarta.json.bind.config.PropertyOrderStrategy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageSerializer {
  private static final JsonbConfig JSONB_CONFIG;

  static {
    JSONB_CONFIG = new JsonbConfig()
        .withFormatting(true)
        .withNullValues(false)
        .withPropertyOrderStrategy(PropertyOrderStrategy.LEXICOGRAPHICAL)
        .withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES)
        .withDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
  }

  public static <T> byte[] serialize(Message<T> object) {
    try(var jsonb = JsonbBuilder.create(JSONB_CONFIG)) {
      var json = jsonb.toJson(object);
      return json.getBytes(StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> Message<T> deserialize(byte[] bytes, Class<T> type) {
    try(var jsonb = JsonbBuilder.create(JSONB_CONFIG)) {
      var parameterizedType = new ParameterizedTypeImpl(Message.class, type);
      var json = new String(bytes, StandardCharsets.UTF_8);
      return jsonb.fromJson(json, parameterizedType);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
