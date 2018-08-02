package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.repository.Compatibility;

import java.io.File;

@NoArgsConstructor
@Getter
@Setter
public class SvnConfigDto extends HalRepresentation {

  private boolean disabled;
  private File repositoryDirectory;

  private boolean enabledGZip;
  private Compatibility compatibility;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
