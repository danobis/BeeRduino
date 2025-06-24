package com.daham.analysis.utils;

import com.daham.analysis.domain.UnitType;
import jakarta.json.bind.adapter.JsonbAdapter;

public class UnitTypeAdapter implements JsonbAdapter<UnitType, String> {
  @Override
  public String adaptToJson(UnitType unitType) {
    return switch (unitType) {
      case DEFAULT -> "unit_default";
      case CELSIUS -> "unit_celsius";
      case PERCENTAGE -> "unit_percentage";
      case GRAM -> "unit_gram";
    };
  }

  @Override
  public UnitType adaptFromJson(String str) {
    return switch (str) {
      case "unit_celsius" -> UnitType.CELSIUS;
      case "unit_percentage" -> UnitType.PERCENTAGE;
      case "unit_gram" -> UnitType.GRAM;
      default -> UnitType.DEFAULT;
    };
  }
}
