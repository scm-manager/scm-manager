package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import org.mapstruct.Mapping;

import java.time.Instant;

public abstract class BaseMapper<T, D extends HalRepresentation> {

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract D map(T modelObject);

  protected Instant mapTime(Long epochMilli) {
    return epochMilli == null? null: Instant.ofEpochMilli(epochMilli);
  }
}
