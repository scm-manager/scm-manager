package sonia.scm.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectMapperProviderTest {

  private final ObjectMapperProvider provider = new ObjectMapperProvider();

  @Test
  void shouldFormatInstantAsISO8601() throws JsonProcessingException {
    ZoneId zone = ZoneId.of("Europe/Berlin");
    ZonedDateTime date = ZonedDateTime.of(2020, 2, 4, 15, 21, 42, 0, zone);

    String value = provider.get().writeValueAsString(date.toInstant());
    assertThat(value).isEqualTo("\"2020-02-04T14:21:42Z\"");
  }

}
