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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;
import sonia.scm.web.EdisonHalAppender;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class RepositoryTypeToRepositoryTypeDtoMapper extends BaseMapper<RepositoryType, RepositoryTypeDto> {

  private static final String REL_IMPORT = "import";

  @Inject
  private ResourceLinks resourceLinks;

  @ObjectFactory
  RepositoryTypeDto create(RepositoryType repositoryType) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.repositoryType().self(repositoryType.getName()));

    if (RepositoryPermissions.create().isPermitted()) {
      if (repositoryType.getSupportedCommands().contains(Command.PULL)) {
        linksBuilder.array(Link.linkBuilder(REL_IMPORT, resourceLinks.repository().importFromUrl(repositoryType.getName())).withName("url").build());
      }
      if (repositoryType.getSupportedCommands().contains(Command.UNBUNDLE)) {
        linksBuilder.array(Link.linkBuilder(REL_IMPORT, resourceLinks.repository().importFromBundle(repositoryType.getName())).withName("bundle").build());
        linksBuilder.array(Link.linkBuilder(REL_IMPORT, resourceLinks.repository().fullImport(repositoryType.getName())).withName("fullImport").build());
      }
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();

    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), repositoryType);

    return new RepositoryTypeDto(linksBuilder.build(), embeddedBuilder.build());
  }

}
