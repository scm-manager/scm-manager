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
import de.otto.edison.hal.Links;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.EdisonHalAppender;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;

import static de.otto.edison.hal.Link.linkBuilder;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class BranchToBranchDtoMapper extends HalAppenderMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Inject
  private RepositoryServiceFactory serviceFactory;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract BranchDto map(Branch branch, @Context NamespaceAndName namespaceAndName);

  abstract PersonDto map(Person person);

  @ObjectFactory
  BranchDto createDto(@Context NamespaceAndName namespaceAndName, Branch branch) {
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.branch().self(namespaceAndName, branch.getName()))
      .single(linkBuilder("history", resourceLinks.branch().history(namespaceAndName, branch.getName())).build())
      .single(linkBuilder("changeset", resourceLinks.changeset().changeset(namespaceAndName.getNamespace(), namespaceAndName.getName(), branch.getRevision())).build())
      .single(linkBuilder("source", resourceLinks.source().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), branch.getRevision())).build());

    Embedded.Builder embeddedBuilder = Embedded.embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), branch, namespaceAndName);
    BranchDto branchDto = new BranchDto(linksBuilder.build(), embeddedBuilder.build());

    try (RepositoryService service = serviceFactory.create(namespaceAndName)) {
      Changeset latestChangeset = service.getLogCommand().setBranch(branch.getName()).getChangesets().getChangesets().get(0);
      branchDto.setLastModified(Instant.ofEpochMilli(latestChangeset.getDate()));
      branchDto.setLastModifier(map(latestChangeset.getAuthor()));
    } catch (IOException e) {
      throw new InternalRepositoryException(
        ContextEntry.ContextBuilder.entity(Branch.class, branch.getName()),
        "Could not read latest changeset for branch",
        e
      );
    }

    return branchDto;
  }
}
