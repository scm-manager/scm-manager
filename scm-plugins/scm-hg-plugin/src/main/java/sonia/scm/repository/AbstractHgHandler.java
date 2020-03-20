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
    
package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;
import sonia.scm.web.HgUtil;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class AbstractHgHandler
{

  /** Field description */
  protected static final String ENV_ID_REVISION = "SCM_ID_REVISION";

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
   */
  protected AbstractHgHandler(HgRepositoryHandler handler, HgContext context,
    Repository repository)
  {
    this(handler, context, repository, handler.getDirectory(repository.getId()));
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
    Map<String, String> env = new HashMap<>();

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
    return createProcess(extraEnv, handler.getConfig().getPythonBinary(),
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

  protected <T> T getResultFromScript(Class<T> resultType, HgPythonScript script) throws IOException {
    return getResultFromScript(resultType, script,
      new HashMap<String, String>());
  }

  @SuppressWarnings("unchecked")
  protected <T> T getResultFromScript(Class<T> resultType,
    HgPythonScript script, Map<String, String> extraEnv)
    throws IOException
  {
    Process p = createScriptProcess(script, extraEnv);

    handleErrorStream(p.getErrorStream());
    try (InputStream input = p.getInputStream()) {
      return  (T) handler.getJaxbContext().createUnmarshaller().unmarshal(input);
    } catch (JAXBException ex) {
      logger.error("could not parse result", ex);

      throw new InternalRepositoryException(repository, "could not parse result", ex);
    }
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

    //J-
    env.put(ENV_ID_REVISION, 
      String.valueOf(handler.getConfig().isShowRevisionInId())
    );
    //J+

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
