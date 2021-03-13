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

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.servlet.ServletModule;
import org.mapstruct.factory.Mappers;
import sonia.scm.api.v2.resources.HgGlobalConfigDtoToHgConfigMapper;
import sonia.scm.api.v2.resources.HgGlobalConfigToHgGlobalConfigDtoMapper;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.spi.HgWorkingCopyFactory;
import sonia.scm.repository.spi.SimpleHgWorkingCopyFactory;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class HgServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    bind(HgGlobalConfigDtoToHgConfigMapper.class).to(Mappers.getMapper(HgGlobalConfigDtoToHgConfigMapper.class).getClass());
    bind(HgGlobalConfigToHgGlobalConfigDtoMapper.class).to(Mappers.getMapper(HgGlobalConfigToHgGlobalConfigDtoMapper.class).getClass());

    bind(HgWorkingCopyFactory.class).to(SimpleHgWorkingCopyFactory.class);
  }
}
