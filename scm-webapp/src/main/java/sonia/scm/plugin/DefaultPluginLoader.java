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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import com.google.inject.Binder;
import com.google.inject.Module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.plugin.ext.AnnotatedClass;
import sonia.scm.plugin.ext.AnnotationCollector;
import sonia.scm.plugin.ext.AnnotationProcessor;
import sonia.scm.plugin.ext.AnnotationScanner;
import sonia.scm.plugin.ext.AnnotationScannerFactory;
import sonia.scm.plugin.ext.DefaultAnnotationScannerFactory;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.plugin.ext.ExtensionBinder;
import sonia.scm.plugin.ext.ExtensionProcessor;
import sonia.scm.plugin.ext.Extensions;
import sonia.scm.web.security.DefaultAuthenticationHandler;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.lang.annotation.Annotation;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultPluginLoader implements PluginLoader
{

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /** Field description */
  public static final String EXTENSION_JAR = ".jar";

  /** Field description */
  public static final String PATH_PLUGINCONFIG = "META-INF/scm/plugin.xml";

  /** Field description */
  public static final String PATH_WEBINFLIB = "/WEB-INF/lib";

  /** Field description */
  public static final String PATH_SCMCORE = PATH_WEBINFLIB.concat("/scm-core");

  /** Field description */
  public static final String REGE_COREPLUGIN =
    "^.*(?:/|\\\\)WEB-INF(?:/|\\\\)lib(?:/|\\\\).*\\.jar$";

  /** the logger for DefaultPluginLoader */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultPluginLoader.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   */
  public DefaultPluginLoader(ServletContext servletContext)
  {
    this.servletContext = servletContext;
    this.annotationScannerFactory = new DefaultAnnotationScannerFactory();

    ClassLoader classLoader = getClassLoader();

    try
    {
      locateCoreFile();
      loadPlugins(classLoader);
      scanForAnnotations();
      findModules();
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param packages
   * @param annotation
   * @param processor
   * @param extensionPointProcessor
   * @param extensionProcessor
   * @param <T>
   *
   * @return
   */
  public <T extends Annotation> AnnotationScanner createAnnotationScanner(
    ClassLoader classLoader, Collection<String> packages,
    AnnotationProcessor<ExtensionPoint> extensionPointProcessor,
    AnnotationProcessor<Extension> extensionProcessor)
  {
    AnnotationScanner scanner = annotationScannerFactory.create(classLoader,
                                  packages);

    if (extensionPointProcessor != null)
    {
      scanner.addProcessor(ExtensionPoint.class, extensionPointProcessor);
    }

    if (extensionProcessor != null)
    {
      scanner.addProcessor(Extension.class, extensionProcessor);
    }

    return scanner;
  }

  /**
   * Method description
   *
   *
   * @param binder
   */
  public void processExtensions(Binder binder)
  {
    new ExtensionBinder(binder).bind(bounds, extensionPoints, extensions);
  }

  /**
   * Method description
   *
   *
   * @param processor
   */
  @Override
  public void processExtensions(ExtensionProcessor processor)
  {
    for (AnnotatedClass<Extension> extension : extensions)
    {
      processor.processExtension(extension.getAnnotation(),
        extension.getAnnotatedClass());
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Plugin> getInstalledPlugins()
  {
    return installedPlugins;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<Module> getModuleSet()
  {
    return moduleSet;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param moduleClass
   */
  private void addModule(Class moduleClass)
  {
    try
    {
      logger.info("add module {}", moduleClass);
      moduleSet.add((Module) moduleClass.newInstance());
    }
    catch (Exception ex)
    {
      logger.error(
        "could not create module instance of ".concat(moduleClass.getName()),
        ex);
    }

  }

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  private String decodePath(String path)
  {
    File file = new File(path);

    if (!file.exists())
    {
      try
      {
        path = URLDecoder.decode(path, ENCODING);
      }
      catch (IOException ex)
      {
        logger.error("could not decode path ".concat(path), ex);
      }
    }

    return path;
  }

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   */
  private String extractResourcePath(URL url)
  {
    String path = url.toExternalForm();

    if (path.startsWith("file:"))
    {
      path = path.substring("file:".length(),
        path.length() - "/META-INF/scm/plugin.xml".length());
    }
    else
    {

      // jar:file:/some/path/file.jar!/META-INF/scm/plugin.xml
      path = path.substring("jar:file:".length(), path.lastIndexOf('!'));
      path = decodePath(path);
    }

    logger.trace("extrace resource path {} from url {}", path, url);

    return path;
  }

  /**
   * Method description
   *
   */
  private void findModules()
  {
    for (AnnotatedClass<Extension> extension : extensions)
    {
      Class extensionClass = extension.getAnnotatedClass();

      if (Module.class.isAssignableFrom(extensionClass))
      {
        bounds.add(extension);
        addModule(extensionClass);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param url
   */
  private void loadPlugin(URL url)
  {
    String path = extractResourcePath(url);

    if (logger.isTraceEnabled())
    {
      logger.trace("try to load plugin from {}", path);
    }

    try
    {
      boolean corePlugin = path.matches(REGE_COREPLUGIN);

      if (logger.isInfoEnabled())
      {
        logger.info("load {}plugin {}", corePlugin
          ? "core "
          : " ", path);
      }

      Plugin plugin = JAXB.unmarshal(url, Plugin.class);
      PluginInformation info = plugin.getInformation();
      PluginCondition condition = plugin.getCondition();

      if (condition != null)
      {
        info.setCondition(condition);
      }

      if (info != null)
      {
        info.setState(corePlugin
          ? PluginState.CORE
          : PluginState.INSTALLED);
      }

      plugin.setPath(path);

      if (logger.isDebugEnabled())
      {
        logger.debug("add plugin {} to installed plugins", info.getId());
      }

      installedPlugins.add(plugin);
    }
    catch (Exception ex)
    {
      logger.error("could not load plugin ".concat(path), ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   *
   * @throws IOException
   */
  private void loadPlugins(ClassLoader classLoader) throws IOException
  {
    Enumeration<URL> urlEnum = classLoader.getResources(PATH_PLUGINCONFIG);

    if (urlEnum != null)
    {
      while (urlEnum.hasMoreElements())
      {
        URL url = urlEnum.nextElement();

        loadPlugin(url);
      }

      if (logger.isDebugEnabled())
      {
        logger.debug("loaded {} plugins", installedPlugins.size());
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("no plugin descriptor found");
    }
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   *
   * @throws MalformedURLException
   */
  private void locateCoreFile() throws MalformedURLException
  {
    Set<String> paths = servletContext.getResourcePaths(PATH_WEBINFLIB);

    for (String path : paths)
    {
      if (path.startsWith(PATH_SCMCORE) && path.endsWith(EXTENSION_JAR))
      {
        coreFile = servletContext.getResource(path);

        break;
      }
    }

    if (coreFile == null)
    {
      throw new IllegalStateException("could not find scm-core file");
    }
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param packageSet
   * @param extensionPointCollector
   * @param extensionCollector
   * @param file
   *
   * @throws IOException
   */
  private void scanFile(ClassLoader classLoader, Collection<String> packageSet,
    AnnotationCollector<ExtensionPoint> extensionPointCollector,
    AnnotationCollector<Extension> extensionCollector, File file)
    throws IOException
  {
    if (logger.isTraceEnabled())
    {
      String type = file.isDirectory()
        ? "directory"
        : "jar";

      logger.trace("search extensions in packages {} of {} file {}",
        new Object[] { packageSet,
        type, file });
    }

    if (file.isDirectory())
    {
      createAnnotationScanner(classLoader, packageSet, extensionPointCollector,
        extensionCollector).scanDirectory(file);
    }
    else
    {
      InputStream input = null;

      try
      {
        input = new FileInputStream(file);
        createAnnotationScanner(classLoader, packageSet,
          extensionPointCollector, extensionCollector).scanArchive(input);
      }
      finally
      {
        Closeables.closeQuietly(input);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param processor
   *
   * @param binder
   */
  private void scanForAnnotations()
  {
    ClassLoader classLoader = getClassLoader();

    AnnotationCollector<ExtensionPoint> extensionPointCollector =
      new AnnotationCollector<ExtensionPoint>();
    AnnotationCollector<Extension> extensionCollector =
      new AnnotationCollector<Extension>();

    logger.debug("search extension points in {}", coreFile);

    Set<String> corePackages = ImmutableSet.of("sonia.scm");

    try
    {
      scanURL(classLoader, corePackages, extensionPointCollector, null,
        coreFile);
    }
    catch (Exception ex)
    {
      throw new IllegalStateException("could not process scm-core", ex);
    }

    for (Plugin plugin : installedPlugins)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("search extensions from plugin {}",
          plugin.getInformation().getId());
      }

      try
      {
        Set<String> packageSet = plugin.getPackageSet();

        if (packageSet == null)
        {
          packageSet = new HashSet<String>();
        }

        packageSet.add(SCMContext.DEFAULT_PACKAGE);

        File pluginFile = new File(plugin.getPath());

        if (pluginFile.exists())
        {
          scanFile(classLoader, packageSet, extensionPointCollector,
            extensionCollector, pluginFile);
        }
        else
        {
          logger.error("could not find plugin file {}", plugin.getPath());
        }
      }
      catch (IOException ex)
      {
        logger.error("error during extension processing", ex);
      }
    }

    //J-
    extensionPoints = extensionPointCollector.getAnnotatedClasses();
    extensionPoints.add(
      new AnnotatedClass<ExtensionPoint>(
        Extensions.createExtensionPoint(true), 
        ServletContextListener.class
      )
    );

    extensions = extensionCollector.getAnnotatedClasses();
    extensions.add( 
      new AnnotatedClass<Extension>(
        Extensions.createExtension(), 
        DefaultAuthenticationHandler.class
      ) 
    );
    //J+
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param packageSet
   * @param extensionPointCollector
   * @param extensionCollector
   * @param file
   *
   * @throws IOException
   */
  private void scanURL(ClassLoader classLoader, Collection<String> packageSet,
    AnnotationCollector<ExtensionPoint> extensionPointCollector,
    AnnotationCollector<Extension> extensionCollector, URL file)
    throws IOException
  {
    InputStream content = null;

    try
    {
      content = file.openStream();
      createAnnotationScanner(classLoader, packageSet, extensionPointCollector,
        extensionCollector).scanArchive(content);
    }
    finally
    {
      Closeables.closeQuietly(content);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * TODO create util method
   *
   *
   * @return
   */
  private ClassLoader getClassLoader()
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    if (classLoader == null)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("could not use context classloader, try to use default");
      }

      classLoader = DefaultPluginManager.class.getClassLoader();
    }

    return classLoader;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private AnnotationScannerFactory annotationScannerFactory;

  /** Field description */
  private Set<AnnotatedClass<Extension>> bounds = Sets.newHashSet();

  /** Field description */
  private URL coreFile;

  /** Field description */
  private Set<AnnotatedClass<ExtensionPoint>> extensionPoints;

  /** Field description */
  private Set<AnnotatedClass<Extension>> extensions;

  /** Field description */
  private Set<Module> moduleSet = Sets.newHashSet();

  /** Field description */
  private Set<Plugin> installedPlugins = new HashSet<Plugin>();

  /** Field description */
  private ServletContext servletContext;
}
