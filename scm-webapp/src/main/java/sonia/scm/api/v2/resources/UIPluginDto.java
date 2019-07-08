package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UIPluginDto extends HalRepresentation {

  private String name;
  private Iterable<String> bundles;
  private String type;
  private String version;
  private String author;
  private String description;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

}
