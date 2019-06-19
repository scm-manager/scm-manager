package sonia.scm.update.properties;

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

  public List<V1Property> getProperties() {
    return unmodifiableList(properties);
  }
}
