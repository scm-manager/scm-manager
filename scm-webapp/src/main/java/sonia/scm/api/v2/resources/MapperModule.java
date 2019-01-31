package sonia.scm.api.v2.resources;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletScopes;
import org.mapstruct.factory.Mappers;

public class MapperModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(UserDtoToUserMapper.class).to(Mappers.getMapper(UserDtoToUserMapper.class).getClass());
    bind(UserToUserDtoMapper.class).to(Mappers.getMapper(UserToUserDtoMapper.class).getClass());
    bind(UserCollectionToDtoMapper.class);

    bind(GroupDtoToGroupMapper.class).to(Mappers.getMapper(GroupDtoToGroupMapper.class).getClass());
    bind(GroupToGroupDtoMapper.class).to(Mappers.getMapper(GroupToGroupDtoMapper.class).getClass());
    bind(GroupCollectionToDtoMapper.class);

    bind(ScmConfigurationToConfigDtoMapper.class).to(Mappers.getMapper(ScmConfigurationToConfigDtoMapper.class).getClass());
    bind(ConfigDtoToScmConfigurationMapper.class).to(Mappers.getMapper(ConfigDtoToScmConfigurationMapper.class).getClass());

    bind(RepositoryToRepositoryDtoMapper.class).to(Mappers.getMapper(RepositoryToRepositoryDtoMapper.class).getClass());
    bind(RepositoryDtoToRepositoryMapper.class).to(Mappers.getMapper(RepositoryDtoToRepositoryMapper.class).getClass());

    bind(RepositoryTypeToRepositoryTypeDtoMapper.class).to(Mappers.getMapper(RepositoryTypeToRepositoryTypeDtoMapper.class).getClass());
    bind(RepositoryTypeCollectionToDtoMapper.class);

    bind(BranchToBranchDtoMapper.class).to(Mappers.getMapper(BranchToBranchDtoMapper.class).getClass());
    bind(RepositoryPermissionDtoToRepositoryPermissionMapper.class).to(Mappers.getMapper(RepositoryPermissionDtoToRepositoryPermissionMapper.class).getClass());
    bind(RepositoryPermissionToRepositoryPermissionDtoMapper.class).to(Mappers.getMapper(RepositoryPermissionToRepositoryPermissionDtoMapper.class).getClass());

    bind(ChangesetToChangesetDtoMapper.class).to(Mappers.getMapper(ChangesetToChangesetDtoMapper.class).getClass());
    bind(ChangesetToParentDtoMapper.class).to(Mappers.getMapper(ChangesetToParentDtoMapper.class).getClass());

    bind(TagToTagDtoMapper.class).to(Mappers.getMapper(TagToTagDtoMapper.class).getClass());

    bind(FileObjectToFileObjectDtoMapper.class).to(Mappers.getMapper(FileObjectToFileObjectDtoMapper.class).getClass());
    bind(ModificationsToDtoMapper.class).to(Mappers.getMapper(ModificationsToDtoMapper.class).getClass());

    bind(ReducedObjectModelToDtoMapper.class).to(Mappers.getMapper(ReducedObjectModelToDtoMapper.class).getClass());

    bind(ResteasyViolationExceptionToErrorDtoMapper.class).to(Mappers.getMapper(ResteasyViolationExceptionToErrorDtoMapper.class).getClass());
    bind(ScmViolationExceptionToErrorDtoMapper.class).to(Mappers.getMapper(ScmViolationExceptionToErrorDtoMapper.class).getClass());
    bind(ExceptionWithContextToErrorDtoMapper.class).to(Mappers.getMapper(ExceptionWithContextToErrorDtoMapper.class).getClass());

    bind(MergeResultToDtoMapper.class).to(Mappers.getMapper(MergeResultToDtoMapper.class).getClass());

    // no mapstruct required
    bind(MeDtoFactory.class);
    bind(UIPluginDtoMapper.class);
    bind(UIPluginDtoCollectionMapper.class);

    bind(ScmPathInfoStore.class).in(ServletScopes.REQUEST);
  }
}
