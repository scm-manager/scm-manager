package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.apache.shiro.SecurityUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.GitConfig;
import sonia.scm.security.Role;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class GitConfigToGitConfigDtoMapper {

  @Inject
  private UriInfoStore uriInfoStore;

  public abstract GitConfigDto map(GitConfig config);

  @AfterMapping
  void appendLinks(GitConfig config, @MappingTarget GitConfigDto target) {
    Links.Builder linksBuilder = linkingTo().self(self());
    // TODO: ConfigPermissions?
    if (SecurityUtils.getSubject().hasRole(Role.ADMIN)) {
      linksBuilder.single(link("update", update()));
    }
    target.add(linksBuilder.build());
  }

  private String self() {
    LinkBuilder linkBuilder = new LinkBuilder(uriInfoStore.get(), GitConfigResource.class);
    return linkBuilder.method("get").parameters().href();
  }

  private String update() {
    LinkBuilder linkBuilder = new LinkBuilder(uriInfoStore.get(), GitConfigResource.class);
    return linkBuilder.method("update").parameters().href();
  }
}
