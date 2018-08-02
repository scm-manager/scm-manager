package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.repository.HgConfig;

@NoArgsConstructor
@Getter
@Setter
public class HgConfigPackageDto extends HalRepresentation {

  private String arch;
  private HgConfig hgConfigTemplate;
  private String hgVersion;
  private String id;
  private String platform;
  private String pythonVersion;
  private long size;
  private String url;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
