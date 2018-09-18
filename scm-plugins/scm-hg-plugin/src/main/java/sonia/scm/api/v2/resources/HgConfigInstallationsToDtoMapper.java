package sonia.scm.api.v2.resources;


import javax.inject.Inject;
import java.util.List;

import static de.otto.edison.hal.Links.linkingTo;

public class HgConfigInstallationsToDtoMapper {

  private ScmPathInfoStore scmPathInfoStore;

  @Inject
  public HgConfigInstallationsToDtoMapper(ScmPathInfoStore scmPathInfoStore) {
    this.scmPathInfoStore = scmPathInfoStore;
  }

  public HgConfigInstallationsDto map(List<String> installations, String path) {
    return new HgConfigInstallationsDto(linkingTo().self(createSelfLink(path)).build(), installations);
  }

  private String createSelfLink(String path) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), HgConfigResource.class);
    return linkBuilder.method("getInstallationsResource").parameters().href() + '/' + path;
  }
}
