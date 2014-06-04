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

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ChildFirstPluginClassLoader;
import sonia.scm.plugin.DefaultPluginClassLoader;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;

import java.util.List;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
public final class BootstrapUtil
{

  /** Field description */
  public static final String CLASSLOADER = "sonia.scm.BoostrapClassLoader";

  /** Field description */
  private static final String STRATEGY =
    "sonia.scm.plugin.classloader.strategy";

  /** Field description */
  private static final String STRATEGY_CHILDFIRST = "child-first";

  /** Field description */
  private static final String STRATEGY_PARENTFIRST = "parent-first";

  /** the logger for BootstrapUtil */
  private static final Logger logger =
    LoggerFactory.getLogger(BootstrapUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private BootstrapUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param classpathURLs
   * @param parent
   *
   * @return
   */
  public static ClassLoader createClassLoader(List<URL> classpathURLs,
    ClassLoader parent)
  {
    ClassLoader classLoader = null;
    URL[] urls = classpathURLs.toArray(new URL[classpathURLs.size()]);
    String strategy = System.getProperty(STRATEGY);

    if (!Strings.isNullOrEmpty(strategy))
    {
      if (STRATEGY_CHILDFIRST.equals(strategy))
      {
        logger.info("using {} as plugin classloading strategy",
          STRATEGY_CHILDFIRST);
        classLoader = new ChildFirstPluginClassLoader(urls, parent);
      }
      else if (!STRATEGY_PARENTFIRST.equals(strategy))
      {
        logger.warn("unknown plugin classloading strategy {}", strategy);
      }
    }

    if (classLoader == null)
    {
      logger.info("using {} as plugin classloading strategy",
        STRATEGY_PARENTFIRST);
      classLoader = new DefaultPluginClassLoader(urls, parent);
    }

    return classLoader;
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param clazz
   * @param <T>
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T loadClass(ClassLoader classLoader, Class<T> clazz)
  {
    T instance = null;

    try
    {
      instance = (T) classLoader.loadClass(clazz.getName()).newInstance();
    }
    catch (Exception ex)
    {
      logger.error("could not load class ".concat(clazz.getName()), ex);
    }

    return instance;
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param clazz
   * @param className
   * @param <T>
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T loadClass(ClassLoader classLoader, Class<T> clazz,
    String className)
  {
    T instance = null;

    try
    {
      instance = (T) classLoader.loadClass(className).newInstance();
    }
    catch (Exception ex)
    {
      logger.error("could not load class ".concat(className), ex);
    }

    return instance;
  }

  /**
   * Method description
   *
   *
   *
   * @param clazz
   * @param className
   * @param <T>
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T loadClass(Class<T> clazz, String className)
  {
    T instance = null;

    try
    {
      instance = (T) Class.forName(className).newInstance();
    }
    catch (Exception ex)
    {
      logger.error("could not load class ".concat(className), ex);
    }

    return instance;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param context
   *
   * @return
   */
  public static ClassLoader getClassLoader(ServletContext context)
  {
    return (ClassLoader) context.getAttribute(CLASSLOADER);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param context
   * @param classLoader
   */
  public static void setClassLoader(ServletContext context,
    ClassLoader classLoader)
  {
    context.setAttribute(CLASSLOADER, classLoader);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param context
   */
  public static void removeClassLoader(ServletContext context)
  {
    context.removeAttribute(CLASSLOADER);
  }
}
