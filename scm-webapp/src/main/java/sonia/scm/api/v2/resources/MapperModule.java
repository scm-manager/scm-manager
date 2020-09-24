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
import sonia.scm.security.gpg.PublicKeyMapper;
import sonia.scm.web.api.RepositoryToHalMapper;

public class MapperModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(UserDtoToUserMapper.class).to(Mappers.getMapperClass(UserDtoToUserMapper.class));
    bind(UserToUserDtoMapper.class).to(Mappers.getMapperClass(UserToUserDtoMapper.class));
    bind(UserCollectionToDtoMapper.class);
    bind(PublicKeyMapper.class).to(Mappers.getMapperClass(PublicKeyMapper.class));

    bind(GroupDtoToGroupMapper.class).to(Mappers.getMapperClass(GroupDtoToGroupMapper.class));
    bind(GroupToGroupDtoMapper.class).to(Mappers.getMapperClass(GroupToGroupDtoMapper.class));
    bind(GroupCollectionToDtoMapper.class);

    bind(ScmConfigurationToConfigDtoMapper.class).to(Mappers.getMapperClass(ScmConfigurationToConfigDtoMapper.class));
    bind(ConfigDtoToScmConfigurationMapper.class).to(Mappers.getMapperClass(ConfigDtoToScmConfigurationMapper.class));

    bind(RepositoryToRepositoryDtoMapper.class).to(Mappers.getMapperClass(RepositoryToRepositoryDtoMapper.class));
    bind(RepositoryDtoToRepositoryMapper.class).to(Mappers.getMapperClass(RepositoryDtoToRepositoryMapper.class));

    bind(RepositoryTypeToRepositoryTypeDtoMapper.class).to(Mappers.getMapperClass(RepositoryTypeToRepositoryTypeDtoMapper.class));
    bind(RepositoryTypeCollectionToDtoMapper.class);

    bind(BranchToBranchDtoMapper.class).to(Mappers.getMapperClass(BranchToBranchDtoMapper.class));
    bind(RepositoryPermissionDtoToRepositoryPermissionMapper.class).to(Mappers.getMapperClass(RepositoryPermissionDtoToRepositoryPermissionMapper.class));
    bind(RepositoryPermissionToRepositoryPermissionDtoMapper.class).to(Mappers.getMapperClass(RepositoryPermissionToRepositoryPermissionDtoMapper.class));

    bind(RepositoryRoleToRepositoryRoleDtoMapper.class).to(Mappers.getMapperClass(RepositoryRoleToRepositoryRoleDtoMapper.class));
    bind(RepositoryRoleDtoToRepositoryRoleMapper.class).to(Mappers.getMapperClass(RepositoryRoleDtoToRepositoryRoleMapper.class));
    bind(RepositoryRoleCollectionToDtoMapper.class);

    bind(ChangesetToChangesetDtoMapper.class).to(Mappers.getMapperClass(DefaultChangesetToChangesetDtoMapper.class));
    bind(ChangesetToParentDtoMapper.class).to(Mappers.getMapperClass(ChangesetToParentDtoMapper.class));

    bind(TagToTagDtoMapper.class).to(Mappers.getMapperClass(TagToTagDtoMapper.class));

    bind(BrowserResultToFileObjectDtoMapper.class).to(Mappers.getMapperClass(BrowserResultToFileObjectDtoMapper.class));
    bind(ModificationsToDtoMapper.class).to(Mappers.getMapperClass(ModificationsToDtoMapper.class));

    bind(ReducedObjectModelToDtoMapper.class).to(Mappers.getMapperClass(ReducedObjectModelToDtoMapper.class));

    bind(ResteasyViolationExceptionToErrorDtoMapper.class).to(Mappers.getMapperClass(ResteasyViolationExceptionToErrorDtoMapper.class));
    bind(ScmViolationExceptionToErrorDtoMapper.class).to(Mappers.getMapperClass(ScmViolationExceptionToErrorDtoMapper.class));
    bind(ExceptionWithContextToErrorDtoMapper.class).to(Mappers.getMapperClass(ExceptionWithContextToErrorDtoMapper.class));

    bind(RepositoryToHalMapper.class).to(Mappers.getMapperClass(RepositoryToRepositoryDtoMapper.class));

    bind(BlameResultToBlameDtoMapper.class).to(Mappers.getMapperClass(BlameResultToBlameDtoMapper.class));
    bind(UpdateInfoMapper.class).to(Mappers.getMapperClass(UpdateInfoMapper.class));

    // no mapstruct required
    bind(MeDtoFactory.class);
    bind(UIPluginDtoMapper.class);
    bind(UIPluginDtoCollectionMapper.class);

    bind(ScmPathInfoStore.class).in(ServletScopes.REQUEST);

    bind(PluginDtoMapper.class).to(Mappers.getMapperClass(PluginDtoMapper.class));
  }
}
