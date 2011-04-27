/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.installer;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.HgConfig;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class UnixHgInstaller extends AbstractHgInstaller
{

  /** Field description */
  public static final String COMMAND_HG = "hg";

  /** Field description */
  public static final String COMMAND_PYTHON = "python";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param baseDirectory
   * @param config
   *
   * @throws IOException
   */
  @Override
  public void install(File baseDirectory, HgConfig config) throws IOException
  {
    super.install(baseDirectory, config);

    if (Util.isEmpty(config.getHgBinary()))
    {
      config.setHgBinary(IOUtil.search(COMMAND_HG));
    }

    if (Util.isEmpty(config.getPythonBinary()))
    {
      config.setPythonBinary(IOUtil.search(COMMAND_PYTHON));
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param baseDirectory
   * @param config
   */
  @Override
  public void update(File baseDirectory, HgConfig config)
  {

    // do nothing
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<String> getHgInstallations()
  {
    return IOUtil.searchAll(COMMAND_HG);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<String> getPythonInstallations()
  {
    return IOUtil.searchAll(COMMAND_PYTHON);
  }
}
