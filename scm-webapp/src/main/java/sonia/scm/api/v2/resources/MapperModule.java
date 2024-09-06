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

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletScopes;
import org.mapstruct.factory.Mappers;
import sonia.scm.web.api.RepositoryToHalMapper;

public class MapperModule extends AbstractModule {
  @Override
  protected void configure() {
    // These mappers are special and cannot be bound automatically by the annotation processor
    bind(ChangesetToChangesetDtoMapper.class).to(Mappers.getMapperClass(DefaultChangesetToChangesetDtoMapper.class));
    bind(RepositoryToHalMapper.class).to(Mappers.getMapperClass(RepositoryToRepositoryDtoMapper.class));

    // no mapstruct required
    bind(MeDtoFactory.class);
    bind(UIPluginDtoMapper.class);
    bind(UIPluginDtoCollectionMapper.class);

    bind(ScmPathInfoStore.class).in(ServletScopes.REQUEST);
  }
}
