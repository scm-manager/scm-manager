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
    
package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects the ServletContainer.
 * This class is inspired by
 * com.liferay.portal.kernel.util.ServerDetector of liferay.
 *
 * @author Sebastian Sdorra
 */
public class ServletContainerDetector
{

  /** Make usage of the logging framework. */
  private static final Logger LOGGER =
    LoggerFactory.getLogger(ServletContainerDetector.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new ServletContainerDetector depending on the ServletRequest.
   * @param req The ServletRequest.
   */
  private ServletContainerDetector(final HttpServletRequest req)
  {
    request = req;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Detects the ServletContainer.
   *
   * @param req The used Servlet instance.
   * @return the detected ServletContainer.
   * @since 1.32
   */
  public static ServletContainer detect(final HttpServletRequest req)
  {
    return new ServletContainerDetector(req).detectContainer();
  }

  /**
   * Detects the ServletContainer.
   *
   *
   * @return the detected ServletContainer
   */
  public ServletContainer detectContainer()
  {
    LOGGER.trace("Detecting servlet container...");

    ServletContainer container = ServletContainer.UNKNOWN;

    if (isScmServer())
    {
      container = ServletContainer.SCM_SERVER;
    }
    else if (isGeronimo())
    {
      container = ServletContainer.GERONIMO;
    }
    else if (isGlassfish())
    {
      container = ServletContainer.GLASSFISH;
    }
    else if (isJBoss())
    {
      container = ServletContainer.JBOSS;
    }
    else if (isJOnAS())
    {
      container = ServletContainer.JONAS;
    }
    else if (isOC4J())
    {
      container = ServletContainer.OC4J;
    }
    else if (isResin())
    {
      container = ServletContainer.RESIN;
    }
    else if (isWebLogic())
    {
      container = ServletContainer.WEBLOGIC;
    }
    else if (isWebSphere())
    {
      container = ServletContainer.WEBSPHERE;
    }
    else if (isJetty())
    {
      container = ServletContainer.JETTY;
    }
    else if (isEclipseJetty())
    {
      container = ServletContainer.ECLIPSE_JETTY;
    }
    else if (isTomcat())
    {
      container = ServletContainer.TOMCAT;
    }

    if (ServletContainer.UNKNOWN.equals(container))
    {
      LOGGER.trace("Servlet container is unknown.");
    }

    return container;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns true if the ServletContainer is a Eclipse Jetty.
   *
   * @since 1.32
   * @return true if the ServletContainer is a Eclipse Jetty
   */
  public boolean isEclipseJetty()
  {
    boolean jetty = detect("/org/eclipse/jetty/server/Server.class");

    if (!jetty && (null != request))
    {
      jetty = detectDefaultServlet("org.eclipse.jetty");
    }

    return jetty;
  }

  /**
   * Returns true if the ServletContainer is a Geronimo.
   *
   *
   * @return true if the ServletContainer is a Geronimo
   */
  public boolean isGeronimo()
  {
    return detect("/org/apache/geronimo/system/main/Daemon.class");
  }

  /**
   * Returns true if the ServletContainer is a Glassfish.
   *
   *
   * @return true if the ServletContainer is a Glassfish
   */
  public boolean isGlassfish()
  {
    final String value = System.getProperty("com.sun.aas.instanceRoot");

    return value != null;
  }

  /**
   * Returns true if the ServletContainer is a JBoss.
   *
   *
   * @return true if the ServletContainer is a JBos
   */
  public boolean isJBoss()
  {
    return detect("/org/jboss/Main.class");
  }

  /**
   * Returns true if the ServletContainer is a JOnAS.
   *
   *
   * @return true if the ServletContainer is a JOnAS
   */
  public boolean isJOnAS()
  {
    boolean jonas = detect("/org/objectweb/jonas/server/Server.class");

    if (!jonas && (System.getProperty("jonas.root") != null))
    {
      jonas = true;
    }

    return jonas;
  }

  /**
   * Returns true if the ServletContainer is a Jetty.
   *
   *
   * @return true if the ServletContainer is a Jetty
   */
  public boolean isJetty()
  {
    return detect("/org/mortbay/jetty/Server.class");
  }

  /**
   * Returns true if the ServletContainer is a OC4J.
   *
   *
   * @return true if the ServletContainer is a OC4J
   */
  public boolean isOC4J()
  {
    return detect("oracle.oc4j.util.ClassUtils");
  }

  /**
   * Returns true if the ServletContainer is a Resin.
   *
   *
   * @return true if the ServletContainer is a Resin
   */
  public boolean isResin()
  {
    return detect("/com/caucho/server/resin/Resin.class");
  }

  /**
   * Returns true if the ServletContainer is a SCM-Server.
   *
   *
   * @return true if the ServletContainer is a SCM-Server
   */
  public boolean isScmServer()
  {
    LOGGER.debug("App name is: " + System.getProperty("app.name"));

    return "scm-server".equals(System.getProperty("app.name"));
  }

  /**
   * Returns true if the ServletContainer is a Tomcat.
   *
   *
   * @return true if the ServletContainer is a Tomcat
   */
  public boolean isTomcat()
  {
    boolean tomcat = detect("/org/apache/catalina/startup/Bootstrap.class");

    if (!tomcat)
    {
      tomcat = detect("/org/apache/catalina/startup/Embedded.class");
    }

    return tomcat;
  }

  /**
   * Returns true if the ServletContainer is a WebLogic.
   *
   *
   * @return true if the ServletContainer is a WebLogic
   */
  public boolean isWebLogic()
  {
    return detect("/weblogic/Server.class");
  }

  /**
   * Returns true if the ServletContainer is a WebSphere.
   *
   *
   * @return true if the ServletContainer is a WebSpere
   */
  public boolean isWebSphere()
  {
    return detect("/com/ibm/websphere/product/VersionInfo.class");
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns true if the given class exists in the system classpath.
   *
   *
   * @param clazz class name to search in classpath
   *
   * @return true if class exists in system classpath
   */
  private boolean detect(final String clazz)
  {
    boolean result = false;
    try
    {
      final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

      systemClassLoader.loadClass(clazz);

      result = true;
    }
    catch (final ClassNotFoundException cnfe)
    {
      final Class<?> classObj = getClass();

      if (classObj.getResource(clazz) != null)
      {
        result = true;
      }
    }
    return result;
  }

  /**
   * An alternate detection. The default servlet that must be implemented by each application, so we can get it's
   * class name and compare against our suggestion.
   *
   * @since 1.32
   * @param keyword Part of the class path that is needed at the implementation class.
   *
   * @return
   */
  private boolean detectDefaultServlet(final String keyword)
  {

    // Request the default servlet (its pretty safe to say it will always be there)
    final RequestDispatcher dispatcher =
      request.getServletContext().getNamedDispatcher("default");

    if (dispatcher == null)
    {
      return false;
    }

    // If the request dispatcher implementation contains the keyword, we can claim a match
    return dispatcher.getClass().getName().contains(keyword);
  }

  //~--- fields ---------------------------------------------------------------

  /** Servlet request for alternate detection method. */
  private HttpServletRequest request = null;
}
