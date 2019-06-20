package sonia.scm.update;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static java.util.Collections.unmodifiableList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "properties")
public class V1Properties {
  @XmlElement(name = "item")
  private List<V1Property> properties;

  public String get(String key) {
    return properties.stream().filter(p -> key.equals(p.getKey())).map(V1Property::getValue).findFirst().orElse(null);
  }
}
