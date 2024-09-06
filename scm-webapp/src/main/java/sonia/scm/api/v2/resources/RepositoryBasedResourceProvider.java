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

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class RepositoryBasedResourceProvider {
  private final Provider<TagRootResource> tagRootResource;
  private final Provider<BranchRootResource> branchRootResource;
  private final Provider<BranchDetailsResource> branchDetailsResource;
  private final Provider<ChangesetRootResource> changesetRootResource;
  private final Provider<SourceRootResource> sourceRootResource;
  private final Provider<ContentResource> contentResource;
  private final Provider<RepositoryPermissionRootResource> permissionRootResource;
  private final Provider<DiffRootResource> diffRootResource;
  private final Provider<ModificationsRootResource> modificationsRootResource;
  private final Provider<FileHistoryRootResource> fileHistoryRootResource;
  private final Provider<IncomingRootResource> incomingRootResource;
  private final Provider<AnnotateResource> annotateResource;
  private final Provider<RepositoryExportResource> repositoryExportResource;
  private final Provider<RepositoryPathsResource> repositoryPathResource;

  @Inject
  public RepositoryBasedResourceProvider(
    Provider<TagRootResource> tagRootResource,
    Provider<BranchRootResource> branchRootResource,
    Provider<BranchDetailsResource> branchDetailsResource,
    Provider<ChangesetRootResource> changesetRootResource,
    Provider<SourceRootResource> sourceRootResource,
    Provider<ContentResource> contentResource,
    Provider<RepositoryPermissionRootResource> permissionRootResource,
    Provider<DiffRootResource> diffRootResource,
    Provider<ModificationsRootResource> modificationsRootResource,
    Provider<FileHistoryRootResource> fileHistoryRootResource,
    Provider<IncomingRootResource> incomingRootResource,
    Provider<AnnotateResource> annotateResource,
    Provider<RepositoryExportResource> repositoryExportResource,
    Provider<RepositoryPathsResource> repositoryPathResource) {
    this.tagRootResource = tagRootResource;
    this.branchRootResource = branchRootResource;
    this.branchDetailsResource = branchDetailsResource;
    this.changesetRootResource = changesetRootResource;
    this.sourceRootResource = sourceRootResource;
    this.contentResource = contentResource;
    this.permissionRootResource = permissionRootResource;
    this.diffRootResource = diffRootResource;
    this.modificationsRootResource = modificationsRootResource;
    this.fileHistoryRootResource = fileHistoryRootResource;
    this.incomingRootResource = incomingRootResource;
    this.annotateResource = annotateResource;
    this.repositoryExportResource = repositoryExportResource;
    this.repositoryPathResource = repositoryPathResource;
  }

  public TagRootResource getTagRootResource() {
    return tagRootResource.get();
  }

  public BranchRootResource getBranchRootResource() {
    return branchRootResource.get();
  }

  public ChangesetRootResource getChangesetRootResource() {
    return changesetRootResource.get();
  }

  public SourceRootResource getSourceRootResource() {
    return sourceRootResource.get();
  }

  public ContentResource getContentResource() {
    return contentResource.get();
  }

  public RepositoryPermissionRootResource getPermissionRootResource() {
    return permissionRootResource.get();
  }

  public DiffRootResource getDiffRootResource() {
    return diffRootResource.get();
  }

  public ModificationsRootResource getModificationsRootResource() {
    return modificationsRootResource.get();
  }

  public FileHistoryRootResource getFileHistoryRootResource() {
    return fileHistoryRootResource.get();
  }

  public IncomingRootResource getIncomingRootResource() {
    return incomingRootResource.get();
  }

  public AnnotateResource getAnnotateResource() {
    return annotateResource.get();
  }

  public RepositoryExportResource getRepositoryExportResource() {
    return repositoryExportResource.get();
  }

  public RepositoryPathsResource getRepositoryPathResource() {
    return repositoryPathResource.get();
  }

  public BranchDetailsResource getBranchDetailsResource() {
    return branchDetailsResource.get();
  }
}
