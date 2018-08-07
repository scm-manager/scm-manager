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
public class HgConfigPackagesDto extends HalRepresentation {

  private List<HgConfigPackageDto> packages;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class HgConfigPackageDto {

    private String arch;
    private HgConfigDto hgConfigTemplate;
    private String hgVersion;
    private String id;
    private String platform;
    private String pythonVersion;
    private long size;
    private String url;
  }
}
