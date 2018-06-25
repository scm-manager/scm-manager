package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import sonia.scm.ModelObject;
import sonia.scm.util.AssertUtil;

import java.time.Instant;
import java.util.Optional;

abstract class BaseMapper<T extends ModelObject, D extends HalRepresentation> {

  public abstract D map(T user);

  Instant mapTime(Long epochMilli) {
    AssertUtil.assertIsNotNull(epochMilli);
    return Instant.ofEpochMilli(epochMilli);
  }

  Optional<Instant> mapOptionalTime(Long epochMilli) {
    return Optional
      .ofNullable(epochMilli)
      .map(Instant::ofEpochMilli);
  }
}
