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

import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import sonia.scm.repository.Branch;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import java.util.Collection;
import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

public class BranchCollectionToDtoMapper {

  private final ResourceLinks resourceLinks;
  private final BranchToBranchDtoMapper branchToDtoMapper;

  @Inject
  public BranchCollectionToDtoMapper(BranchToBranchDtoMapper branchToDtoMapper, ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
    this.branchToDtoMapper = branchToDtoMapper;
  }

  public HalRepresentation map(Repository repository, Collection<Branch> branches) {
    return new HalRepresentation(
      createLinks(repository),
      embedDtos(getBranchDtoList(repository, branches)));
  }

  public List<BranchDto> getBranchDtoList(Repository repository, Collection<Branch> branches) {
    return branches.stream().map(branch -> branchToDtoMapper.map(branch, repository)).collect(toList());
  }

  private Links createLinks(Repository repository) {
    String namespace = repository.getNamespace();
    String name = repository.getName();
    String baseUrl = resourceLinks.branchCollection().self(namespace, name);

    Links.Builder linksBuilder = linkingTo().with(createSelfLink(baseUrl));
    if (RepositoryPermissions.push(repository).isPermitted()) {
      linksBuilder.single(createCreateLink(namespace, name));
    }
    return linksBuilder.build();
  }

  private Links createSelfLink(String baseUrl) {
    return Links.linkingTo().self(baseUrl).build();
  }

  private Link createCreateLink(String namespace, String name) {
    return Link.link("create", resourceLinks.branch().create(namespace, name));
  }

  private Embedded embedDtos(List<BranchDto> dtos) {
    return embeddedBuilder()
      .with("branches", dtos)
      .build();
  }
}
