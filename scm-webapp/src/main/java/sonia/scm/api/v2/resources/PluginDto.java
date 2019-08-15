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
  private String version;
  private String displayName;
  private String description;
  private String author;
  private String category;
  private String avatarUrl;

  public PluginDto(Links links) {
    add(links);
  }
}
