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
