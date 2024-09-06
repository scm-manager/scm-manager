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

import sonia.scm.repository.HealthCheckService;
import sonia.scm.repository.RepositoryManager;

import static com.google.inject.util.Providers.of;

abstract class RepositoryTestBase {

  RepositoryToRepositoryDtoMapper repositoryToDtoMapper;
  RepositoryDtoToRepositoryMapper dtoToRepositoryMapper;
  RepositoryManager manager;
  TagRootResource tagRootResource;
  BranchRootResource branchRootResource;
  BranchDetailsResource branchDetailsResource;
  ChangesetRootResource changesetRootResource;
  SourceRootResource sourceRootResource;
  ContentResource contentResource;
  RepositoryPermissionRootResource permissionRootResource;
  DiffRootResource diffRootResource;
  ModificationsRootResource modificationsRootResource;
  FileHistoryRootResource fileHistoryRootResource;
  IncomingRootResource incomingRootResource;
  RepositoryCollectionResource repositoryCollectionResource;
  AnnotateResource annotateResource;
  RepositoryImportResource repositoryImportResource;
  RepositoryExportResource repositoryExportResource;
  RepositoryPathsResource repositoryPathsResource;
  HealthCheckService healthCheckService;

  RepositoryRootResource getRepositoryRootResource() {
    RepositoryBasedResourceProvider repositoryBasedResourceProvider = new RepositoryBasedResourceProvider(
      of(tagRootResource),
      of(branchRootResource),
      of(branchDetailsResource),
      of(changesetRootResource),
      of(sourceRootResource),
      of(contentResource),
      of(permissionRootResource),
      of(diffRootResource),
      of(modificationsRootResource),
      of(fileHistoryRootResource),
      of(incomingRootResource),
      of(annotateResource),
      of(repositoryExportResource),
      of(repositoryPathsResource)
    );
    return new RepositoryRootResource(
      of(new RepositoryResource(
        repositoryToDtoMapper,
        dtoToRepositoryMapper,
        manager,
        repositoryBasedResourceProvider,
        healthCheckService)),
      of(repositoryCollectionResource),
      of(repositoryImportResource));
  }
}
