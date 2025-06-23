package com.daham.rest.json;

import com.daham.utils.UnitTypeAdapter;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

@JsonbTypeAdapter(UnitTypeAdapter.class)
public enum UnitType {
  DEFAULT,
  CELSIUS,
  PERCENTAGE,
  GRAM,
}
