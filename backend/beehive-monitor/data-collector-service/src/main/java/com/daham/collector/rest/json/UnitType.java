package com.daham.collector.rest.json;

import com.daham.collector.utils.UnitTypeAdapter;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

@JsonbTypeAdapter(UnitTypeAdapter.class)
public enum UnitType {
  DEFAULT,
  CELSIUS,
  PERCENTAGE,
  GRAM,
}
