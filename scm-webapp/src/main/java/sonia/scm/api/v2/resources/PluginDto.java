package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;

import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PluginDto extends HalRepresentation {

  private String name;
  private String type;
  private String version;
  private String author;
  private String description;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

}
