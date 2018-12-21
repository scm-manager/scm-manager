package sonia.scm.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * JAXB adapter for {@link Instant} objects.
 *
 * @since 2.0.0
 */
public class XmlInstantAdapter extends XmlAdapter<String, Instant> {

  @Override
  public String marshal(Instant instant) {
    return DateTimeFormatter.ISO_INSTANT.format(instant);
  }

  @Override
  public Instant unmarshal(String text) {
    TemporalAccessor parsed = DateTimeFormatter.ISO_INSTANT.parse(text);
    return Instant.from(parsed);
  }
}
