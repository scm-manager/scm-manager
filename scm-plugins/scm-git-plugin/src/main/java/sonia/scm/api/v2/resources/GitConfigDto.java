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

  private File repositoryDirectory;
  private boolean disabled = false;

  private String gcExpression;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
