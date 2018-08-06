package sonia.scm.api.v2.resources;

import com.google.inject.Inject;

import java.util.List;

import static de.otto.edison.hal.Links.linkingTo;

public class HgConfigInstallationsToDtoMapper {

  private UriInfoStore uriInfoStore;

  @Inject
  public HgConfigInstallationsToDtoMapper(UriInfoStore uriInfoStore, String path) {
    this.uriInfoStore = uriInfoStore;
  }

  public HgConfigInstallationsDto map(List<String> installations, String path) {
    return new HgConfigInstallationsDto(linkingTo().self(createSelfLink(path)).build(), installations);
  }

  private String createSelfLink(String path) {
    LinkBuilder linkBuilder = new LinkBuilder(uriInfoStore.get(), HgConfigResource.class);
    return linkBuilder.method("getInstallationsResource").parameters().href() + '/' + path;
  }
}
