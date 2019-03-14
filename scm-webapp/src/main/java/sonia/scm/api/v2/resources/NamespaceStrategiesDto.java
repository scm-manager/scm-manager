package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;

import java.util.List;

@Getter
public class NamespaceStrategiesDto extends HalRepresentation {

  private String current;
  private List<String> available;

  public NamespaceStrategiesDto(String current, List<String> available, Links links) {
    super(links);
    this.current = current;
    this.available = available;
  }
}
