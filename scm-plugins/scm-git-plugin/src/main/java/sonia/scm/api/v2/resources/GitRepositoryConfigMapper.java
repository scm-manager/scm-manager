/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class GitRepositoryConfigMapper {

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
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
