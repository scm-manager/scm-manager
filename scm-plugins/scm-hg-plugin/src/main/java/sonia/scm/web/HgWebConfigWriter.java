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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.io.RegexResourceProcessor;
import sonia.scm.io.ResourceProcessor;
import sonia.scm.repository.HgConfig;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgWebConfigWriter
{

  /** Field description */
  public static final String CGI_TEMPLATE = "/sonia/scm/hgweb.py";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param config
   */
  public HgWebConfigWriter(HgConfig config)
  {
    this.config = config;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @throws IOException
   */
  public void write() throws IOException
  {
    File cgiFile = HgUtil.getCGI();

    writeCGIFile(cgiFile);
  }

  /**
   * Method description
   *
   *
   * @param cgiFile
   *
   * @throws IOException
   */
  private void writeCGIFile(File cgiFile) throws IOException
  {
    InputStream input = null;
    OutputStream output = null;

    try
    {
      input = HgWebConfigWriter.class.getResourceAsStream(CGI_TEMPLATE);
      output = new FileOutputStream(cgiFile);

      ResourceProcessor rp = new RegexResourceProcessor();

      rp.addVariable("python", config.getPythonBinary());
      rp.process(input, output);
      cgiFile.setExecutable(true);
    }
    finally
    {
      IOUtil.close(input);
      IOUtil.close(output);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgConfig config;
}
