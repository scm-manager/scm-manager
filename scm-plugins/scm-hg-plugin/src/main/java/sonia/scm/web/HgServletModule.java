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
import sonia.scm.api.v2.resources.HgConfigDtoToHgConfigMapper;
import sonia.scm.api.v2.resources.HgConfigInstallationsToDtoMapper;
import sonia.scm.api.v2.resources.HgConfigPackagesToDtoMapper;
import sonia.scm.api.v2.resources.HgConfigToHgConfigDtoMapper;
import sonia.scm.installer.HgPackageReader;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.HgContext;
import sonia.scm.repository.HgContextProvider;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.spi.HgWorkdirFactory;
import sonia.scm.repository.spi.SimpleHgWorkdirFactory;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class HgServletModule extends ServletModule
{

  /** Field description */
  public static final String MAPPING_HG = "/hg/*";

  /** Field description */
  public static final String MAPPING_HOOK = "/hook/hg/*";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void configureServlets()
  {
    bind(HgContext.class).toProvider(HgContextProvider.class);
    bind(HgHookManager.class);
    bind(HgPackageReader.class);

    bind(HgConfigDtoToHgConfigMapper.class).to(Mappers.getMapper(HgConfigDtoToHgConfigMapper.class).getClass());
    bind(HgConfigToHgConfigDtoMapper.class).to(Mappers.getMapper(HgConfigToHgConfigDtoMapper.class).getClass());
    bind(HgConfigPackagesToDtoMapper.class).to(Mappers.getMapper(HgConfigPackagesToDtoMapper.class).getClass());
    bind(HgConfigInstallationsToDtoMapper.class);

    // bind servlets
    serve(MAPPING_HOOK).with(HgHookCallbackServlet.class);

    bind(HgWorkdirFactory.class).to(SimpleHgWorkdirFactory.class);
  }
}
