package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.apache.shiro.SecurityUtils;

import javax.inject.Inject;

public class IndexDtoGenerator {

  private final ResourceLinks resourceLinks;

  @Inject
  public IndexDtoGenerator(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  public IndexDto generate() {
    Links.Builder builder = Links.linkingTo();
    if (SecurityUtils.getSubject().isAuthenticated()) {
      builder.single(
        Link.link("me", resourceLinks.me().self()),
        Link.link("logout", resourceLinks.authentication().logout())
      );
    } else {
      builder.single(
        Link.link("formLogin", resourceLinks.authentication().formLogin()),
        Link.link("jsonLogin", resourceLinks.authentication().jsonLogin())
      );
    }

    return new IndexDto(builder.build());
  }
}
