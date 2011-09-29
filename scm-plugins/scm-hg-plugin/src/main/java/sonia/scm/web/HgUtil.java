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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgUtil
{

  /** Field description */
  public static final String CGI_DIRECTORY = "cgi-bin";

  /** Field description */
  public static final String CGI_NAME = "hgweb.py";

  /** Field description */
  public static final String ENV_PATH = "SCM_PATH";

  /** Field description */
  public static final String ENV_PYTHON_PATH = "SCM_PYTHON_PATH";

  /** Field description */
  public static final String ENV_REPOSITORY_PATH = "SCM_REPOSITORY_PATH";

  /** Field description */
  public static final String ENV_REVISION = "SCM_REVISION";

  /** Field description */
  public static final String REVISION_TIP = "tip";

  /** the logger for HgUtil */
  private static final Logger logger = LoggerFactory.getLogger(HgUtil.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param handler
   * @param directory
   * @param extraEnv
   *
   * @return
   *
   * @throws IOException
   */
  public static Process createPythonProcess(HgRepositoryHandler handler,
          File directory, Map<String, String> extraEnv)
          throws IOException
  {
    HgConfig config = handler.getConfig();
    ProcessBuilder pb = new ProcessBuilder(config.getPythonBinary());
    Map<String, String> env = pb.environment();

    env.put(ENV_PYTHON_PATH, Util.nonNull(config.getPythonPath()));
    env.put(ENV_REPOSITORY_PATH, directory.getAbsolutePath());
    env.putAll(extraEnv);

    return pb.start();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static File getCGI()
  {
    File cgiDirectory = new File(SCMContext.getContext().getBaseDirectory(),
                                 CGI_DIRECTORY);

    if (!cgiDirectory.exists() &&!cgiDirectory.mkdirs())
    {
      throw new RuntimeException(
          "could not create directory".concat(cgiDirectory.getPath()));
    }

    return new File(cgiDirectory, CGI_NAME);
  }

  /**
   * Method description
   *
   *
   * @param resultType
   * @param context
   * @param scriptResource
   * @param handler
   * @param directory
   * @param extraEnv
   * @param <T>
   *
   * @return
   *
   * @throws IOException
   */
  public static <T> T getResultFromScript(Class<T> resultType,
          JAXBContext context, String scriptResource,
          HgRepositoryHandler handler, File directory,
          Map<String, String> extraEnv)
          throws IOException
  {
    Process p = createPythonProcess(handler, directory, extraEnv);
    T result = null;
    InputStream resource = null;
    InputStream input = null;
    OutputStream output = null;

    try
    {
      resource = HgUtil.class.getResourceAsStream(scriptResource);
      output = p.getOutputStream();
      IOUtil.copy(resource, output);
      output.close();
      input = p.getInputStream();
      result = (T) context.createUnmarshaller().unmarshal(input);
      input.close();
    }
    catch (JAXBException ex)
    {
      logger.error("could not parse result", ex);
    }
    finally
    {
      IOUtil.close(resource);
      IOUtil.close(input);
      IOUtil.close(output);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param resultType
   * @param context
   * @param scriptResource
   * @param handler
   * @param repository
   * @param extraEnv
   * @param <T>
   *
   * @return
   *
   * @throws IOException
   */
  public static <T> T getResultFromScript(Class<T> resultType,
          JAXBContext context, String scriptResource,
          HgRepositoryHandler handler, Repository repository,
          Map<String, String> extraEnv)
          throws IOException
  {
    File directory = handler.getDirectory(repository);

    return getResultFromScript(resultType, context, scriptResource, handler,
                               directory, extraEnv);
  }

  /**
   * Method description
   *
   *
   * @param resultType
   * @param context
   * @param scriptResource
   * @param handler
   * @param repository
   * @param revision
   * @param path
   * @param <T>
   *
   * @return
   *
   * @throws IOException
   */
  public static <T> T getResultFromScript(Class<T> resultType,
          JAXBContext context, String scriptResource,
          HgRepositoryHandler handler, Repository repository, String revision,
          String path)
          throws IOException
  {
    Map<String, String> extraEnv = new HashMap<String, String>();

    extraEnv.put(ENV_REVISION, getRevision(revision));
    extraEnv.put(ENV_PATH, Util.nonNull(path));

    return getResultFromScript(resultType, context, scriptResource, handler,
                               repository, extraEnv);
  }

  /**
   * Method description
   *
   *
   * @param revision
   *
   * @return
   */
  public static String getRevision(String revision)
  {
    return Util.isEmpty(revision)
           ? REVISION_TIP
           : revision;
  }
}
