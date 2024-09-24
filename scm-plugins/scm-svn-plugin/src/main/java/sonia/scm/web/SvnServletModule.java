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

package sonia.scm.web;

import com.google.inject.servlet.ServletModule;
import org.mapstruct.factory.Mappers;
import sonia.scm.api.v2.resources.SvnConfigDtoToSvnConfigMapper;
import sonia.scm.api.v2.resources.SvnConfigToSvnConfigDtoMapper;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.SvnWorkingCopyFactory;
import sonia.scm.repository.spi.SimpleSvnWorkingCopyFactory;


@Extension
public class SvnServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    bind(SvnConfigDtoToSvnConfigMapper.class).to(Mappers.getMapper(SvnConfigDtoToSvnConfigMapper.class).getClass());
    bind(SvnConfigToSvnConfigDtoMapper.class).to(Mappers.getMapper(SvnConfigToSvnConfigDtoMapper.class).getClass());
    bind(SvnWorkingCopyFactory.class).to(SimpleSvnWorkingCopyFactory.class);
  }
}
