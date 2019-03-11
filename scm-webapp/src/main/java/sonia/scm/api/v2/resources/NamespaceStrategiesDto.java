package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NamespaceStrategiesDto extends HalRepresentation {

  private String current;
  private List<String> available;

  public NamespaceStrategiesDto(Links links) {
    super(links);
  }
}
