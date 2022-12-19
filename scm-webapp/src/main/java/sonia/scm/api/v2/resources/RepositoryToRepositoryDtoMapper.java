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
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.DefaultRepositoryExportingCheck;
import sonia.scm.repository.Feature;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.HealthCheckService;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SearchableType;
import sonia.scm.web.EdisonHalAppender;
import sonia.scm.web.api.RepositoryToHalMapper;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Link.linkBuilder;
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
  @Inject
  private HealthCheckService healthCheckService;
  @Inject
  private SCMContextProvider contextProvider;
  @Inject
  private SearchEngine searchEngine;

  abstract HealthCheckFailureDto toDto(HealthCheckFailure failure);

  @ObjectFactory
  HealthCheckFailureDto createHealthCheckFailureDto(HealthCheckFailure failure) {
    String url = failure.getUrl(contextProvider.getDocumentationVersion());
    if (url == null) {
      return new HealthCheckFailureDto();
    } else {
      return new HealthCheckFailureDto(Links.linkingTo().single(link("documentation", url)).build());
    }
  }

  @AfterMapping
  void updateHealthCheckUrlForCurrentVersion(HealthCheckFailure failure, @MappingTarget HealthCheckFailureDto dto) {
    dto.setUrl(failure.getUrl(contextProvider.getDocumentationVersion()));
  }

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  @Mapping(target = "exporting", ignore = true)
  @Override
  public abstract RepositoryDto map(Repository modelObject);

  @AfterMapping
  void setExporting(Repository repository, @MappingTarget RepositoryDto repositoryDto) {
    repositoryDto.setExporting(DefaultRepositoryExportingCheck.isRepositoryExporting(repository.getId()));
  }

  @ObjectFactory
  RepositoryDto createDto(Repository repository) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.repository().self(repository.getNamespace(), repository.getName()));
    if (RepositoryPermissions.custom("*", repository).isPermitted()) {
      linksBuilder.single(link("reindex", resourceLinks.repository().reindex(repository.getNamespace(), repository.getName())));
    }
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

      if (repositoryService.isSupported(Command.BUNDLE) && RepositoryPermissions.export(repository).isPermitted()) {
        linksBuilder.single(link("export", resourceLinks.repository().export(repository.getNamespace(), repository.getName(), repository.getType())));
        linksBuilder.single(link("fullExport", resourceLinks.repository().fullExport(repository.getNamespace(), repository.getName(), repository.getType())));
        linksBuilder.single(link("exportInfo", resourceLinks.repository().exportInfo(repository.getNamespace(), repository.getName())));
      }

      if (repositoryService.isSupported(Command.TAGS)) {
        linksBuilder.single(link("tags", resourceLinks.tag().all(repository.getNamespace(), repository.getName())));
      }
      if (repositoryService.isSupported(Command.BRANCHES)) {
        linksBuilder.single(link("branches", resourceLinks.branchCollection().self(repository.getNamespace(), repository.getName())));
      }
      if (repositoryService.isSupported(Command.BRANCH_DETAILS)) {
        linksBuilder.single(link("branchDetailsCollection", resourceLinks.branchDetailsCollection().self(repository.getNamespace(), repository.getName())));
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
    linksBuilder.single(link("content", resourceLinks.source().content(repository.getNamespace(), repository.getName())));
    linksBuilder.single(link("paths", resourceLinks.repository().paths(repository.getNamespace(), repository.getName())));
    if (RepositoryPermissions.healthCheck(repository).isPermitted() && !healthCheckService.checkRunning(repository)) {
      linksBuilder.single(link("runHealthCheck", resourceLinks.repository().runHealthCheck(repository.getNamespace(), repository.getName())));
    }
    linksBuilder.single(link("searchableTypes", resourceLinks.searchableTypes().searchableTypesForRepository(repository.getNamespace(), repository.getName())));
    linksBuilder.array(searchLinks(repository.getNamespace(), repository.getName()));

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), repository);

    RepositoryDto repositoryDto = new RepositoryDto(linksBuilder.build(), embeddedBuilder.build());
    repositoryDto.setHealthCheckRunning(healthCheckService.checkRunning(repository));
    return repositoryDto;
  }

  private List<Link> searchLinks(String namespace, String name) {
    return searchEngine.getSearchableTypes().stream()
      .filter(SearchableType::limitableToRepository)
      .map(SearchableType::getName)
      .map(typeName ->
        linkBuilder("search", resourceLinks.search().queryForRepository(namespace, name, typeName)).withName(typeName).build()
      )
      .collect(Collectors.toList());
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
