package sonia.scm.api.v2.resources;

import com.google.inject.util.Providers;
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
  protected Provider<RepositoryPermissionRootResource> permissionRootResource;
  protected Provider<DiffRootResource> diffRootResource;
  protected Provider<ModificationsRootResource> modificationsRootResource;
  protected Provider<FileHistoryRootResource> fileHistoryRootResource;
  protected Provider<RepositoryCollectionResource> repositoryCollectionResource;
  protected Provider<IncomingRootResource> incomingRootResource;
  protected Provider<MergeResource> mergeResource;


  RepositoryRootResource getRepositoryRootResource() {
    return new RepositoryRootResource(Providers.of(new RepositoryResource(
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
      fileHistoryRootResource,
      incomingRootResource,
      mergeResource)), repositoryCollectionResource);
  }


}
