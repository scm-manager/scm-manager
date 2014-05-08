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

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.util.ClassLoaders;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
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
      logger.info("destroy scm context listener");
      scmContextListener.contextDestroyed(sce);
    }

    ServletContext servletContext = sce.getServletContext();
    ClassLoader classLoader = BootstrapUtil.getClassLoader(servletContext);

    if (classLoader != null)
    {
      if (classLoader instanceof Closeable)
      {
        logger.info("close plugin class loader");
        IOUtil.close((Closeable) classLoader);
      }

      logger.debug("remove plugin class loader from servlet context");
      BootstrapUtil.removeClassLoader(servletContext);
    }
    else
    {
      logger.debug("plugin class loader is not available");
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
    SCMContextProvider context = SCMContext.getContext();

    if (logger.isInfoEnabled())
    {
      logger.info("start scm-manager {} in stage: {}", context.getVersion(),
        context.getStage());
    }

    ClassLoader classLoader = createClassLoader(context);

    if (classLoader != null)
    {
      if (logger.isInfoEnabled())
      {
        logger.info("try to use ScmBootstrapClassLoader");
      }

      scmContextListener = BootstrapUtil.loadClass(classLoader,
        ServletContextListener.class, LISTENER);
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

    initializeContext(classLoader, scmContextListener, sce);
  }

  /**
   * Method description
   *
   *
   * @param context
   *
   * @return
   */
  private ClassLoader createClassLoader(SCMContextProvider context)
  {
    ClassLoader classLoader = null;
    File pluginDirectory = new File(context.getBaseDirectory(),
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
          else if (logger.isErrorEnabled())
          {
            logger.error("classloader is null");
          }
        }
        catch (Exception ex)
        {
          logger.error("could not load classpath from plugin folder", ex);
        }
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("no plugin directory found");
    }

    return classLoader;
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
    if (logger.isDebugEnabled())
    {
      logger.debug("create classloader from plugin classpath");
    }

    List<URL> classpathURLs = Lists.newLinkedList();

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
          URL url = file.toURI().toURL();

          if (logger.isDebugEnabled())
          {
            logger.debug("append {} to classpath", url.toExternalForm());
          }

          classpathURLs.add(url);
        }
        catch (MalformedURLException ex)
        {
          logger.error("could not append url to classpath", ex);
        }
      }
      else if (logger.isErrorEnabled())
      {
        logger.error("plugin file {} does not exists", file);
      }
    }

    return BootstrapUtil.createClassLoader(classpathURLs,
      ClassLoaders.getContextClassLoader(BootstrapListener.class));
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param listener
   * @param sce
   */
  private void initializeContext(ClassLoader classLoader,
    ServletContextListener listener, ServletContextEvent sce)
  {
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

    try
    {
      if (classLoader != null)
      {
        Thread.currentThread().setContextClassLoader(classLoader);
      }

      logger.info("initialize scm context listener");
      listener.contextInitialized(sce);
    }
    finally
    {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ServletContextListener scmContextListener;
}
