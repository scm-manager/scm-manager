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

package sonia.scm.trace;

import com.google.common.collect.ImmutableMap;
import jakarta.xml.bind.JAXB;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SpanContextTest {

  @Test
  void shouldMarshalAndUnmarshal() {
    Instant now = Instant.now();
    SpanContext span = new SpanContext(
      "jenkins", ImmutableMap.of("one", "1"), now, now, true
    );

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JAXB.marshal(span, baos);
    span = JAXB.unmarshal(new ByteArrayInputStream(baos.toByteArray()), SpanContext.class);

    assertThat(span.getKind()).isEqualTo("jenkins");
    assertThat(span.label("one")).isEqualTo("1");
    assertThat(span.getOpened()).isEqualTo(now);
    assertThat(span.getClosed()).isEqualTo(now);
    assertThat(span.isFailed()).isTrue();
  }

}
