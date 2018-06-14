package sonia.scm.api.v2.resources;

import sonia.scm.util.AssertUtil;

import java.time.Instant;
import java.util.Optional;

class BaseMapper {

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
