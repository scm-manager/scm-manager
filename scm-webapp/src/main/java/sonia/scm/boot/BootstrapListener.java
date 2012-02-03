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



package sonia.scm.boot;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
public class BootstrapListener implements ServletContextListener
{

  /** Field description */
  public static final String LISTENER = "sonia.scm.ScmContextListener";

  /** Field description */
  public static final String PLUGIN_CLASSPATHFILE = "classpath.xml";

  /** Field description */
  public static final String PLUGIN_DIRECTORY = "plugins";

  /** the logger for BootstrapListener */
  private static final Logger logger =
    LoggerFactory.getLogger(BootstrapListener.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param sce
   */
  @Override
  public void contextDestroyed(ServletContextEvent sce)
  {
    if (scmContextListener != null)
    {
      scmContextListener.contextDestroyed(sce);
    }
  }

  /**
   * Method description
   *
   *
   * @param sce
   */
  @Override
  public void contextInitialized(ServletContextEvent sce)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("start scm-manager in stage: {}",
                  SCMContext.getContext().getStage());
    }

    ClassLoader classLoader = null;
    File pluginDirectory = new File(SCMContext.getContext().getBaseDirectory(),
                                    PLUGIN_DIRECTORY);

    if (pluginDirectory.exists())
    {
      File classpathFile = new File(pluginDirectory, PLUGIN_CLASSPATHFILE);

      if (classpathFile.exists())
      {
        try
        {
          Classpath classpath = JAXB.unmarshal(classpathFile, Classpath.class);

          if (classpath != null)
          {
            classLoader = createClassLoader(pluginDirectory, classpath);
          }
        }
        catch (Exception ex)
        {
          logger.error(ex.getMessage(), ex);
        }
      }
    }

    if (classLoader != null)
    {
      if (logger.isInfoEnabled())
      {
        logger.info("try to use ScmBootstrapClassLoader");
      }

      scmContextListener = BootstrapUtil.loadClass(classLoader,
              ServletContextListener.class, LISTENER);
      Thread.currentThread().setContextClassLoader(classLoader);
      BootstrapUtil.setClassLoader(sce.getServletContext(), classLoader);
    }

    if (scmContextListener == null)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("fallback to default classloader");
      }

      scmContextListener =
        BootstrapUtil.loadClass(ServletContextListener.class, LISTENER);
    }

    scmContextListener.contextInitialized(sce);
  }

  /**
   * Method description
   *
   *
   * @param pluginDirectory
   * @param classpath
   *
   * @return
   */
  private ClassLoader createClassLoader(File pluginDirectory,
          Classpath classpath)
  {
    List<URL> classpathURLs = new LinkedList<URL>();

    for (String path : classpath)
    {
      if (path.startsWith("/"))
      {
        path = path.substring(1);
      }

      File file = new File(pluginDirectory, path);

      if (file.exists())
      {
        try
        {
          if (logger.isDebugEnabled())
          {
            logger.debug("append {} to classpath", file.getPath());
          }

          classpathURLs.add(file.toURI().toURL());
        }
        catch (MalformedURLException ex)
        {
          logger.error(ex.getMessage(), ex);
        }
      }
    }

    return new URLClassLoader(classpathURLs.toArray(new URL[0]),
                              getParentClassLoader());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private ClassLoader getParentClassLoader()
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    if (classLoader == null)
    {
      classLoader = BootstrapListener.class.getClassLoader();
    }

    return classLoader;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ServletContextListener scmContextListener;
}
