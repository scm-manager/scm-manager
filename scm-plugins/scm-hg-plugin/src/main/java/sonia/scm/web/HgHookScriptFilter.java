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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.io.RegexResourceProcessor;
import sonia.scm.io.ResourceProcessor;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;
import sonia.scm.web.filter.HttpFilter;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class HgHookScriptFilter extends HttpFilter
{

  /** Field description */
  public static final String SCRIPT_TEMPLATE = "/sonia/scm/hghook.py";

  /** the logger for HgHookScriptFilter */
  private static final Logger logger =
    LoggerFactory.getLogger(HgHookScriptFilter.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param handler
   */
  @Inject
  public HgHookScriptFilter(SCMContextProvider context,
                            HgRepositoryHandler handler)
  {
    this.context = context;
    this.handler = handler;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param chain
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doFilter(HttpServletRequest request,
                          HttpServletResponse response, FilterChain chain)
          throws IOException, ServletException
  {
    if (!written)
    {
      synchronized (HgHookScriptFilter.class)
      {
        if (!written)
        {
          writeHookScript(request);
          written = true;
        }
      }
    }

    chain.doFilter(request, response);
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @throws IOException
   */
  private void writeHookScript(HttpServletRequest request) throws IOException
  {
    StringBuilder url = new StringBuilder(request.getScheme());

    url.append("://localhost:").append(request.getServerPort());
    url.append(request.getContextPath()).append("/api/rest/hook/hg/");

    File cgiDirectory = new File(context.getBaseDirectory(), "cgi-bin");

    IOUtil.mkdirs(cgiDirectory);

    File hgHookScript = new File(cgiDirectory, "scmhooks.py");

    if (logger.isDebugEnabled())
    {
      logger.debug("write hg hook script to '{}'", hgHookScript);
    }

    writeScript(hgHookScript, url.toString());
  }

  /**
   * Method description
   *
   *
   *
   * @param script
   * @param url
   *
   * @throws IOException
   */
  private void writeScript(File script, String url) throws IOException
  {
    InputStream input = null;
    OutputStream output = null;

    try
    {
      input = HgWebConfigWriter.class.getResourceAsStream(SCRIPT_TEMPLATE);
      output = new FileOutputStream(script);

      HgConfig config = handler.getConfig();
      ResourceProcessor rp = new RegexResourceProcessor();

      rp.addVariable("python", config.getPythonBinary());
      rp.addVariable("path", Util.nonNull(config.getPythonPath()));
      rp.addVariable("url", url);
      rp.process(input, output);
      script.setExecutable(true);
    }
    finally
    {
      IOUtil.close(input);
      IOUtil.close(output);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SCMContextProvider context;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private volatile boolean written = false;
}
