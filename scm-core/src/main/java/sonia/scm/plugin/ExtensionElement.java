package sonia.scm.plugin;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashSet;
import java.util.Set;

@Getter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtensionElement {
  @XmlElement(name = "class")
  private String clazz;
  private String description;
  private Set<String> requires = new HashSet<>();
}
