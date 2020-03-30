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

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletScopes;
import org.mapstruct.factory.Mappers;
import sonia.scm.web.api.RepositoryToHalMapper;

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

    bind(RepositoryRoleToRepositoryRoleDtoMapper.class).to(Mappers.getMapper(RepositoryRoleToRepositoryRoleDtoMapper.class).getClass());
    bind(RepositoryRoleDtoToRepositoryRoleMapper.class).to(Mappers.getMapper(RepositoryRoleDtoToRepositoryRoleMapper.class).getClass());
    bind(RepositoryRoleCollectionToDtoMapper.class);

    bind(ChangesetToChangesetDtoMapper.class).to(Mappers.getMapper(DefaultChangesetToChangesetDtoMapper.class).getClass());
    bind(ChangesetToParentDtoMapper.class).to(Mappers.getMapper(ChangesetToParentDtoMapper.class).getClass());

    bind(TagToTagDtoMapper.class).to(Mappers.getMapper(TagToTagDtoMapper.class).getClass());

    bind(BrowserResultToFileObjectDtoMapper.class).to(Mappers.getMapper(BrowserResultToFileObjectDtoMapper.class).getClass());
    bind(ModificationsToDtoMapper.class).to(Mappers.getMapper(ModificationsToDtoMapper.class).getClass());

    bind(ReducedObjectModelToDtoMapper.class).to(Mappers.getMapper(ReducedObjectModelToDtoMapper.class).getClass());

    bind(ResteasyViolationExceptionToErrorDtoMapper.class).to(Mappers.getMapper(ResteasyViolationExceptionToErrorDtoMapper.class).getClass());
    bind(ScmViolationExceptionToErrorDtoMapper.class).to(Mappers.getMapper(ScmViolationExceptionToErrorDtoMapper.class).getClass());
    bind(ExceptionWithContextToErrorDtoMapper.class).to(Mappers.getMapper(ExceptionWithContextToErrorDtoMapper.class).getClass());

    bind(RepositoryToHalMapper.class).to(Mappers.getMapper(RepositoryToRepositoryDtoMapper.class).getClass());

    // no mapstruct required
    bind(MeDtoFactory.class);
    bind(UIPluginDtoMapper.class);
    bind(UIPluginDtoCollectionMapper.class);

    bind(ScmPathInfoStore.class).in(ServletScopes.REQUEST);

    bind(PluginDtoMapper.class).to(Mappers.getMapper(PluginDtoMapper.class).getClass());
  }
}
