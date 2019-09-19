package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("squid:S2160") // we do not need equals for dto
public class PluginDto extends HalRepresentation {

  private String name;
  private String version;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String newVersion;
  private String displayName;
  private String description;
  private String author;
  private String category;
  private String avatarUrl;
  private boolean pending;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean core;
  private Boolean markedForUninstall;
  private Set<String> dependencies;

  public PluginDto(Links links) {
    add(links);
  }
}
