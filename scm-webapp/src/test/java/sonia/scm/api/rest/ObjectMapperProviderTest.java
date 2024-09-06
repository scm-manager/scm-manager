/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.otto.edison.hal.HalRepresentation;
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

  @Test
  void shouldDeserializeEmbeddedObjects() throws JsonProcessingException {
    HalRepresentation halRepresentation = provider.get().readValue("{\"_embedded\": {\"avatar\": {\"type\": \"AUTO_GENERATED\"}}}", HalRepresentation.class);

    assertThat(halRepresentation.getEmbedded().getItemsBy("avatar")).hasSize(1);
  }
}
