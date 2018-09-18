package sonia.scm.api.v2.resources;

import java.time.Instant;

public interface InstantAttributeMapper {
  default Instant mapTime(Long epochMilli) {
    return epochMilli == null? null: Instant.ofEpochMilli(epochMilli);
  }
}
