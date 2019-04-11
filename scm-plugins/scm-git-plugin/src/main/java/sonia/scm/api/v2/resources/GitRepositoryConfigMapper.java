package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class GitRepositoryConfigMapper {

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  public abstract GitRepositoryConfigDto map(GitRepositoryConfig config, @Context Repository repository);
  public abstract GitRepositoryConfig map(GitRepositoryConfigDto dto);

  @AfterMapping
  void appendLinks(@MappingTarget GitRepositoryConfigDto target, @Context Repository repository) {
    Links.Builder linksBuilder = linkingTo().self(self());
    if (RepositoryPermissions.custom("git", repository).isPermitted()) {
      linksBuilder.single(link("update", update()));
    }
    target.add(linksBuilder.build());
  }

  private String self() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), GitConfigResource.class);
    return linkBuilder.method("get").parameters().href();
  }

  private String update() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), GitConfigResource.class);
    return linkBuilder.method("update").parameters().href();
  }
}
