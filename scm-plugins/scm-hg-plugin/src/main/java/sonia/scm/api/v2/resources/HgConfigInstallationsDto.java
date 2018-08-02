package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class HgConfigInstallationsDto extends HalRepresentation {

  public HgConfigInstallationsDto(Links links, List<String> paths) {
    super(links);
    this.paths = paths;
  }

  private List<String> paths;
}
