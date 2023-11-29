/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
