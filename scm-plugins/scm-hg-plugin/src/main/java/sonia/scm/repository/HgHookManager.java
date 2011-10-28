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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.io.RegexResourceProcessor;
import sonia.scm.io.ResourceProcessor;
import sonia.scm.util.IOUtil;
import sonia.scm.web.HgWebConfigWriter;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class HgHookManager
{

  /** Field description */
  public static final String SCRIPT_TEMPLATE = "/sonia/scm/hghook.py";

  /** the logger for HgHookManager */
  private static final Logger logger =
    LoggerFactory.getLogger(HgHookManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   */
  @Inject
  public HgHookManager(SCMContextProvider context)
  {
    this.context = context;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * TODO check if file exists
   *
   *
   * @param request
   *
   * @throws IOException
   */
  public void writeHookScript(HttpServletRequest request) throws IOException
  {
    if (isScriptWriteAble())
    {
      synchronized (this)
      {
        if (isScriptWriteAble())
        {
          StringBuilder url = new StringBuilder(request.getScheme());

          url.append("://localhost:").append(request.getLocalPort());
          url.append(request.getContextPath()).append("/hook/hg/");

          if (hgHookScript == null)
          {
            File cgiDirectory = new File(context.getBaseDirectory(), "cgi-bin");

            IOUtil.mkdirs(cgiDirectory);
            hgHookScript = new File(cgiDirectory, "scmhooks.py");
          }

          if (logger.isDebugEnabled())
          {
            logger.debug("write hg hook script for '{}' to '{}'", url,
                         hgHookScript);
          }

          writeScript(hgHookScript, url.toString());
        }
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getChallenge()
  {
    return challenge;
  }

  /**
   * Method description
   *
   *
   * @param challenge
   *
   * @return
   */
  public boolean isAcceptAble(String challenge)
  {
    return this.challenge.equals(challenge);
  }

  //~--- methods --------------------------------------------------------------

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

      ResourceProcessor rp = new RegexResourceProcessor();

      rp.addVariable("url", url);
      rp.addVariable("challenge", getChallenge());
      rp.process(input, output);
      script.setExecutable(true);
    }
    finally
    {
      IOUtil.close(input);
      IOUtil.close(output);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private boolean isScriptWriteAble()
  {
    return (hgHookScript == null) ||!hgHookScript.exists();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String challenge = UUID.randomUUID().toString();

  /** Field description */
  private SCMContextProvider context;

  /** Field description */
  private volatile File hgHookScript;
}
