/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
