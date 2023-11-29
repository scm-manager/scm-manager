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

package sonia.scm.xml;

import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XmlInstantAdapterTest {

  @Test
  void shouldMarshalAndUnmarshalInstant(@TempDir Path tempDirectory) {
    Path path = tempDirectory.resolve("instant.xml");

    Instant instant = Instant.now();
    InstantObject object = new InstantObject(instant);
    JAXB.marshal(object, path.toFile());

    InstantObject unmarshaled = JAXB.unmarshal(path.toFile(), InstantObject.class);
    assertEquals(instant, unmarshaled.instant);
  }

  @XmlRootElement(name = "instant-object")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class InstantObject {

    @XmlJavaTypeAdapter(XmlInstantAdapter.class)
    private Instant instant;

    public InstantObject() {
    }

    InstantObject(Instant instant) {
      this.instant = instant;
    }
  }

}
