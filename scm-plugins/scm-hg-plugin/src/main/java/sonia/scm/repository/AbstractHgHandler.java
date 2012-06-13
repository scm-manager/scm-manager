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

import com.google.common.io.Closeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;
import sonia.scm.web.HgUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

/**
 *
 * @author Sebastian Sdorra
 */
public class AbstractHgHandler
{

  /** Field description */
  protected static final String ENV_NODE = "HG_NODE";

  /** Field description */
  protected static final String ENV_PAGE_LIMIT = "SCM_PAGE_LIMIT";

  /** Field description */
  protected static final String ENV_PAGE_START = "SCM_PAGE_START";

  /** Field description */
  protected static final String ENV_PATH = "SCM_PATH";

  /** Field description */
  protected static final String ENV_REPOSITORY_PATH = "SCM_REPOSITORY_PATH";

  /** Field description */
  protected static final String ENV_REVISION = "SCM_REVISION";

  /** Field description */
  protected static final String ENV_REVISION_END = "SCM_REVISION_END";

  /** Field description */
  protected static final String ENV_REVISION_START = "SCM_REVISION_START";

  /** Field description */
  private static final String ENCODING = "UTF-8";

  /** mercurial encoding */
  private static final String ENV_HGENCODING = "HGENCODING";

  /** Field description */
  private static final String ENV_PENDING = "HG_PENDING";

  /** python encoding */
  private static final String ENV_PYTHONIOENCODING = "PYTHONIOENCODING";

  /** Field description */
  private static final String ENV_PYTHONPATH = "PYTHONPATH";

  /**
   * the logger for AbstractHgCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractHgHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param handler
   * @param context
   * @param repository
   * @param repositoryDirectory
   */
  protected AbstractHgHandler(HgRepositoryHandler handler, HgContext context,
                              Repository repository)
  {
    this(handler, context, repository, handler.getDirectory(repository));
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param handler
   * @param context
   * @param repository
   * @param repositoryDirectory
   */
  protected AbstractHgHandler(HgRepositoryHandler handler, HgContext context,
                              Repository repository, File repositoryDirectory)
  {
    this.handler = handler;
    this.context = context;
    this.repository = repository;
    this.repositoryDirectory = repositoryDirectory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   * @param path
   *
   * @return
   */
  protected Map<String, String> createEnvironment(String revision, String path)
  {
    Map<String, String> env = new HashMap<String, String>();

    env.put(ENV_REVISION, HgUtil.getRevision(revision));
    env.put(ENV_PATH, Util.nonNull(path));

    return env;
  }

  /**
   * Method description
   *
   *
   * @param args
   *
   * @return
   *
   * @throws IOException
   */
  protected Process createHgProcess(String... args) throws IOException
  {
    return createHgProcess(new HashMap<String, String>(), args);
  }

  /**
   * Method description
   *
   *
   * @param extraEnv
   * @param args
   *
   * @return
   *
   * @throws IOException
   */
  protected Process createHgProcess(Map<String, String> extraEnv,
                                    String... args)
          throws IOException
  {
    return createProcess(extraEnv, handler.getConfig().getHgBinary(), args);
  }

  /**
   * Method description
   *
   *
   * @param script
   * @param extraEnv
   *
   * @return
   *
   * @throws IOException
   */
  protected Process createScriptProcess(HgPythonScript script,
          Map<String, String> extraEnv)
          throws IOException
  {
    return createProcess(
        extraEnv, handler.getConfig().getPythonBinary(),
        script.getFile(SCMContext.getContext()).getAbsolutePath());
  }

  /**
   * Method description
   *
   *
   * @param errorStream
   */
  protected void handleErrorStream(final InputStream errorStream)
  {
    if (errorStream != null)
    {
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            String content = IOUtil.getContent(errorStream);

            if (Util.isNotEmpty(content))
            {
              logger.error(content.trim());
            }
          }
          catch (IOException ex)
          {
            logger.error("error during logging", ex);
          }
        }
      }).start();
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param resultType
   * @param script
   * @param <T>
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  protected <T> T getResultFromScript(Class<T> resultType,
          HgPythonScript script)
          throws IOException, RepositoryException
  {
    return getResultFromScript(resultType, script,
                               new HashMap<String, String>());
  }

  /**
   * Method description
   *
   *
   * @param resultType
   * @param script
   * @param extraEnv
   * @param <T>
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  protected <T> T getResultFromScript(Class<T> resultType,
          HgPythonScript script, Map<String, String> extraEnv)
          throws IOException, RepositoryException
  {
    Process p = createScriptProcess(script, extraEnv);
    T result = null;
    InputStream input = null;
    OutputStream output = null;

    try
    {
      handleErrorStream(p.getErrorStream());
      input = p.getInputStream();
      result =
        (T) handler.getJaxbContext().createUnmarshaller().unmarshal(input);
      input.close();
    }
    catch (JAXBException ex)
    {
      logger.error("could not parse result", ex);

      throw new RepositoryException("could not parse result", ex);
    }
    finally
    {
      Closeables.closeQuietly(input);
      Closeables.closeQuietly(output);
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param extraEnv
   * @param cmd
   * @param args
   *
   * @return
   *
   * @throws IOException
   */
  private Process createProcess(Map<String, String> extraEnv, String cmd,
                                String... args)
          throws IOException
  {
    HgConfig config = handler.getConfig();
    List<String> cmdList = new ArrayList<String>();

    cmdList.add(cmd);

    if (Util.isNotEmpty(args))
    {
      cmdList.addAll(Arrays.asList(args));
    }

    if (logger.isDebugEnabled())
    {
      StringBuilder msg = new StringBuilder("create process for [");
      Iterator<String> it = cmdList.iterator();

      while (it.hasNext())
      {
        msg.append(it.next());

        if (it.hasNext())
        {
          msg.append(", ");
        }
      }

      msg.append("]");
      logger.debug(msg.toString());
    }

    ProcessBuilder pb = new ProcessBuilder(cmdList);

    pb.directory(repositoryDirectory);

    Map<String, String> env = pb.environment();

    // force utf-8 encoding for mercurial and python
    env.put(ENV_PYTHONIOENCODING, ENCODING);
    env.put(ENV_HGENCODING, ENCODING);

    if (context.isSystemEnvironment())
    {
      env.putAll(System.getenv());
    }

    if (context.isPending())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("enable hg pending for {}",
                     repositoryDirectory.getAbsolutePath());
      }

      env.put(ENV_PENDING, repositoryDirectory.getAbsolutePath());

      if (extraEnv.containsKey(ENV_REVISION_START))
      {
        env.put(ENV_NODE, extraEnv.get(ENV_REVISION_START));
      }
    }

    env.put(ENV_PYTHONPATH, HgUtil.getPythonPath(config));
    env.put(ENV_REPOSITORY_PATH, repositoryDirectory.getAbsolutePath());
    env.putAll(extraEnv);

    if (logger.isTraceEnabled())
    {
      StringBuilder msg = new StringBuilder("start process in directory '");

      msg.append(repositoryDirectory.getAbsolutePath()).append(
          "' with env: \n");

      for (Map.Entry<String, String> e : env.entrySet())
      {
        msg.append("  ").append(e.getKey());
        msg.append(" = ").append(e.getValue());
        msg.append("\n");
      }

      logger.trace(msg.toString());
    }

    return pb.start();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected Repository repository;

  /** Field description */
  protected File repositoryDirectory;

  /** Field description */
  private HgContext context;

  /** Field description */
  private HgRepositoryHandler handler;
}
