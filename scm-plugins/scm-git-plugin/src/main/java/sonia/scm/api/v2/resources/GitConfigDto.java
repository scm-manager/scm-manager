package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;

@NoArgsConstructor
@Getter
@Setter
public class GitConfigDto extends SimpleRepositoryConfigDto {

  @XmlElement(name = "gc-expression")
  private String gcExpression;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
