package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HgConfigInstallationsDto extends HalRepresentation {

  private List<String> paths;

  public HgConfigInstallationsDto(Links links, List<String> paths) {
    super(links);
    this.paths = paths;
  }

}
