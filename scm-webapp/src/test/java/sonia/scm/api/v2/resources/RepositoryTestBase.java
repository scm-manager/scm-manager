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

import sonia.scm.repository.RepositoryManager;

import static com.google.inject.util.Providers.of;

abstract class RepositoryTestBase {

  RepositoryToRepositoryDtoMapper repositoryToDtoMapper;
  RepositoryDtoToRepositoryMapper dtoToRepositoryMapper;
  RepositoryManager manager;
  TagRootResource tagRootResource;
  BranchRootResource branchRootResource;
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

  RepositoryRootResource getRepositoryRootResource() {
    RepositoryBasedResourceProvider repositoryBasedResourceProvider = new RepositoryBasedResourceProvider(
      of(tagRootResource),
      of(branchRootResource),
      of(changesetRootResource),
      of(sourceRootResource),
      of(contentResource),
      of(permissionRootResource),
      of(diffRootResource),
      of(modificationsRootResource),
      of(fileHistoryRootResource),
      of(incomingRootResource),
      of(annotateResource),
      of(repositoryExportResource));
    return new RepositoryRootResource(
      of(new RepositoryResource(
        repositoryToDtoMapper,
        dtoToRepositoryMapper,
        manager,
        repositoryBasedResourceProvider)),
            of(repositoryCollectionResource), of(repositoryImportResource));
  }
}
