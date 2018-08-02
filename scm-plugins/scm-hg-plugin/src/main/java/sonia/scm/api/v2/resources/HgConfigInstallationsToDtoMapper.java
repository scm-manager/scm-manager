package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;

import javax.inject.Inject;
import java.util.List;

import static de.otto.edison.hal.Links.linkingTo;

public class HgConfigInstallationsToDtoMapper {
  @Inject private UriInfoStore uriInfoStore;

  public HalRepresentation map(List<String> installations) {
    return new HgConfigInstallationsDto(linkingTo().self(createSelfLink()).build(), installations);
  }

  private String createSelfLink() {
    LinkBuilder linkBuilder = new LinkBuilder(uriInfoStore.get(), HgConfigInstallationsResource.class);
    return linkBuilder.method("get").parameters().href();
  }
}
