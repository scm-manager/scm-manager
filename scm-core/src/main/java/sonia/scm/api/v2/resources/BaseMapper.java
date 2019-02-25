package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import org.mapstruct.Mapping;

public abstract class BaseMapper<T, D extends HalRepresentation> extends HalAppenderMapper implements InstantAttributeMapper {

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract D map(T modelObject);
}
