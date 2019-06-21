package sonia.scm.update;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "properties")
public class V1Properties {
  @XmlElement(name = "item")
  private List<V1Property> properties;

  public V1Properties() {
  }

  public V1Properties(V1Property... properties) {
    this(asList(properties));
  }

  public V1Properties(List<V1Property> properties) {
    this.properties = properties;
  }

  public String get(String key) {
    return properties.stream().filter(p -> key.equals(p.getKey())).map(V1Property::getValue).findFirst().orElse(null);
  }

  public boolean hasAny(String[] keys) {
    return properties.stream().anyMatch(p -> stream(keys).anyMatch(k -> k.equals(p.getKey())));
  }

  public boolean hasAll(String[] keys) {
    return stream(keys).allMatch(k -> properties.stream().anyMatch(p -> k.equals(p.getKey())));
  }
}
