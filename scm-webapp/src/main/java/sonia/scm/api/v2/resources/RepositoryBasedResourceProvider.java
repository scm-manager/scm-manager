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
