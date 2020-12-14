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
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Feature;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;
import sonia.scm.web.EdisonHalAppender;
import sonia.scm.web.api.RepositoryToHalMapper;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class RepositoryToRepositoryDtoMapper extends BaseMapper<Repository, RepositoryDto> implements RepositoryToHalMapper {

  @Inject
  private ResourceLinks resourceLinks;
  @Inject
  private ScmConfiguration scmConfiguration;
  @Inject
  private RepositoryServiceFactory serviceFactory;
  @Inject
  private Set<NamespaceStrategy> strategies;

  abstract HealthCheckFailureDto toDto(HealthCheckFailure failure);

  @Override
  public abstract RepositoryDto map(Repository modelObject);

  @ObjectFactory
  RepositoryDto createDto(Repository repository) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.repository().self(repository.getNamespace(), repository.getName()));
    if (RepositoryPermissions.delete(repository).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.repository().delete(repository.getNamespace(), repository.getName())));
    }
    if (RepositoryPermissions.modify(repository).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.repository().update(repository.getNamespace(), repository.getName())));
    }
    if (RepositoryPermissions.archive().isPermitted(repository)) {
      if (repository.isArchived()) {
        linksBuilder.single(link("unarchive", resourceLinks.repository().unarchive(repository.getNamespace(), repository.getName())));
      } else {
        linksBuilder.single(link("archive", resourceLinks.repository().archive(repository.getNamespace(), repository.getName())));
      }
    }
    if (RepositoryPermissions.rename(repository).isPermitted()) {
      if (isRenameNamespacePossible()) {
        linksBuilder.single(link("renameWithNamespace", resourceLinks.repository().rename(repository.getNamespace(), repository.getName())));
      } else {
        linksBuilder.single(link("rename", resourceLinks.repository().rename(repository.getNamespace(), repository.getName())));
      }
    }
    if (RepositoryPermissions.permissionRead(repository).isPermitted()) {
      linksBuilder.single(link("permissions", resourceLinks.repositoryPermission().all(repository.getNamespace(), repository.getName())));
    }
    try (RepositoryService repositoryService = serviceFactory.create(repository)) {
      if (RepositoryPermissions.pull(repository).isPermitted()) {
        List<Link> protocolLinks = repositoryService.getSupportedProtocols()
          .map(this::createProtocolLink)
          .collect(toList());
        linksBuilder.array(protocolLinks);
      }
      if (repositoryService.isSupported(Command.TAGS)) {
        linksBuilder.single(link("tags", resourceLinks.tag().all(repository.getNamespace(), repository.getName())));
      }
      if (repositoryService.isSupported(Command.BRANCHES)) {
        linksBuilder.single(link("branches", resourceLinks.branchCollection().self(repository.getNamespace(), repository.getName())));
      }
      if (repositoryService.isSupported(Feature.INCOMING_REVISION)) {
        linksBuilder.single(link("incomingChangesets", resourceLinks.incoming().changesets(repository.getNamespace(), repository.getName())));
        linksBuilder.single(link("incomingDiff", resourceLinks.incoming().diff(repository.getNamespace(), repository.getName())));
        if (repositoryService.isSupported(Command.DIFF_RESULT)) {
          linksBuilder.single(link("incomingDiffParsed", resourceLinks.incoming().diffParsed(repository.getNamespace(), repository.getName())));
        }
      }
    }
    linksBuilder.single(link("changesets", resourceLinks.changeset().all(repository.getNamespace(), repository.getName())));
    linksBuilder.single(link("sources", resourceLinks.source().selfWithoutRevision(repository.getNamespace(), repository.getName())));

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), repository);

    return new RepositoryDto(linksBuilder.build(), embeddedBuilder.build());
  }

  private boolean isRenameNamespacePossible() {
    for (NamespaceStrategy strategy : strategies) {
      if (strategy.getClass().getSimpleName().equals(scmConfiguration.getNamespaceStrategy())) {
        return strategy.canBeChanged();
      }
    }
    return false;
  }

  private Link createProtocolLink(ScmProtocol protocol) {
    return Link.linkBuilder("protocol", protocol.getUrl()).withName(protocol.getType()).build();
  }
}
