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
public class HgConfigDto extends HalRepresentation {

  private boolean disabled;
  private File repositoryDirectory;

  private String encoding;
  private String hgBinary;
  private String pythonBinary;
  private String pythonPath;
  private boolean useOptimizedBytecode;
  private boolean showRevisionInId;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
