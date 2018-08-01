package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

@NoArgsConstructor
@Getter
@Setter
public class GitConfigDto extends HalRepresentation {

  private String gcExpression;
  private File repositoryDirectory;
  private boolean disabled = false;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
