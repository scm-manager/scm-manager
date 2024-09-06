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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.admin.ScmConfigurationStore;
import sonia.scm.repository.DefaultRepositoryExportingCheck;
import sonia.scm.repository.Feature;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.HealthCheckService;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SearchableType;
import sonia.scm.web.EdisonHalAppender;
import sonia.scm.web.api.RepositoryToHalMapper;

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
@Slf4j
public abstract class RepositoryToRepositoryDtoMapper extends BaseMapper<Repository, RepositoryDto> implements RepositoryToHalMapper {

  @Inject
  private ResourceLinks resourceLinks;
  @Inject
  private ScmConfigurationStore scmConfigurationStore;
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
    } catch (RepositoryLocationResolver.LocationNotFoundException e) {
      // This might happen, when the repository has been deleted in the meantime
      // There is nothing we can do here, so we just log this
      log.warn("could not create repository service for repository; the repository might have been deleted", e);
    }
    linksBuilder.single(link("changesets", resourceLinks.changeset().all(repository.getNamespace(), repository.getName())));
    linksBuilder.single(link("sources", resourceLinks.source().selfWithoutRevision(repository.getNamespace(), repository.getName())));
    linksBuilder.single(link("content", resourceLinks.source().content(repository.getNamespace(), repository.getName())));
    if (scmConfigurationStore.get().isEnabledFileSearch()) {
      linksBuilder.single(link("paths", resourceLinks.repository().paths(repository.getNamespace(), repository.getName())));
    }
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
      if (strategy.getClass().getSimpleName().equals(scmConfigurationStore.get().getNamespaceStrategy())) {
        return strategy.canBeChanged();
      }
    }
    return false;
  }

  private Link createProtocolLink(ScmProtocol protocol) {
    return Link.linkBuilder("protocol", protocol.getUrl()).withName(protocol.getType()).withProfile(String.valueOf(protocol.getPriority())).build();
  }
}
