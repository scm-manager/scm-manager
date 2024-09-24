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
