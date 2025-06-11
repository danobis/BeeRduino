package com.daham.client.model;

import com.daham.client.utils.adapters.UnitTypeAdapter;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

@JsonbTypeAdapter(UnitTypeAdapter.class)
public enum UnitType {
  UNIT_DEFAULT,
  UNIT_CELSIUS,
  UNIT_PERCENTAGE,
  UNIT_GRAM,
}
