package sonia.scm.api.v2.resources;

import java.time.Instant;

class BaseDtoMapper {
  Long mapDate(Instant value) {
    if (value != null) {
      return value.toEpochMilli();
    }
    return null;
  }
}
