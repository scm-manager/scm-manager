package sonia.scm.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TempDirectory.class)
class XmlInstantAdapterTest {

  @Test
  void shouldMarshalAndUnmarshalInstant(@TempDirectory.TempDir Path tempDirectory) {
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
