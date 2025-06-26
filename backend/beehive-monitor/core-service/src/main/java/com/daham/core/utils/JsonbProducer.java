package com.daham.core.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyNamingStrategy;
import jakarta.json.bind.config.PropertyOrderStrategy;

import java.util.Locale;

@ApplicationScoped
public class JsonbProducer {
  @Produces
  @Dependent
  public Jsonb produceJsonb() {
    var jsonConfig = new JsonbConfig()
        .withFormatting(true)
        .withNullValues(false)
        .withPropertyOrderStrategy(PropertyOrderStrategy.LEXICOGRAPHICAL)
        .withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES)
        .withDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
    return JsonbBuilder.create(jsonConfig);
  }
}
