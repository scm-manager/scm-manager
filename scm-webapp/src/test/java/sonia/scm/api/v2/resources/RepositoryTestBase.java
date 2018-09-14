package sonia.scm.api.v2.resources;

import sonia.scm.repository.RepositoryManager;

import javax.inject.Provider;

public abstract class RepositoryTestBase {


  protected RepositoryToRepositoryDtoMapper repositoryToDtoMapper;
  protected RepositoryDtoToRepositoryMapper dtoToRepositoryMapper;
  protected RepositoryManager manager;
  protected Provider<TagRootResource> tagRootResource;
  protected Provider<BranchRootResource> branchRootResource;
  protected Provider<ChangesetRootResource> changesetRootResource;
  protected Provider<SourceRootResource> sourceRootResource;
  protected Provider<ContentResource> contentResource;
  protected Provider<PermissionRootResource> permissionRootResource;
  protected Provider<DiffRootResource> diffRootResource;
  protected Provider<ModificationsRootResource> modificationsRootResource;
  protected Provider<FileHistoryRootResource> fileHistoryRootResource;
  protected Provider<RepositoryCollectionResource> repositoryCollectionResource;


  RepositoryRootResource getRepositoryRootResource() {
    return new RepositoryRootResource(MockProvider.of(new RepositoryResource(
      repositoryToDtoMapper,
      dtoToRepositoryMapper,
      manager,
      tagRootResource,
      branchRootResource,
      changesetRootResource,
      sourceRootResource,
      contentResource,
      permissionRootResource,
      diffRootResource,
      modificationsRootResource,
      fileHistoryRootResource)), repositoryCollectionResource);
  }


}
