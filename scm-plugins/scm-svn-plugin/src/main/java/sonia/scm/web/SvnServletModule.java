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
