package com.daham.client.utils.adapters;

import com.daham.client.model.UnitType;
import jakarta.json.bind.adapter.JsonbAdapter;

public class UnitTypeAdapter implements JsonbAdapter<UnitType, String> {
  @Override
  public String adaptToJson(UnitType unitType) {
    return switch (unitType) {
      case UNIT_DEFAULT -> "unit_default";
      case UNIT_CELSIUS -> "unit_celsius";
      case UNIT_PERCENTAGE -> "unit_percentage";
      case UNIT_GRAM -> "unit_gram";
    };
  }

  @Override
  public UnitType adaptFromJson(String str) {
    return switch (str) {
      case "unit_celsius" -> UnitType.UNIT_CELSIUS;
      case "unit_percentage" -> UnitType.UNIT_PERCENTAGE;
      case "unit_gram" -> UnitType.UNIT_GRAM;
      default -> UnitType.UNIT_DEFAULT;
    };
  }
}
