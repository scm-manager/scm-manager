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



package sonia.scm.plugin.ext;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.lang.annotation.Annotation;

import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultAnnotationScanner implements AnnotationScanner
{

  /**
   * the logger for DefaultAnnotationScanner
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultAnnotationScanner.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param classLoader
   * @param packages
   */
  public DefaultAnnotationScanner(ClassLoader classLoader,
    Collection<String> packages)
  {
    this.classLoader = classLoader;
    this.packages = packages;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param annotationClass
   * @param processor
   * @param <T>
   */
  @Override
  public <T extends Annotation> void addProcessor(Class<T> annotationClass,
    AnnotationProcessor<T> processor)
  {
    processors.put(annotationClass, processor);
  }

  /**
   * Method description
   *
   *
   * @param archive
   *
   * @throws IOException
   */
  @Override
  public void scanArchive(InputStream archive) throws IOException
  {
    JarInputStream input = null;

    try
    {
      input = new JarInputStream(archive);

      JarEntry entry = input.getNextJarEntry();

      while (entry != null)
      {
        if (!entry.isDirectory())
        {
          processEntry(entry);
        }

        entry = input.getNextJarEntry();
      }
    }
    finally
    {
      IOUtil.close(input);
    }
  }

  /**
   * Method description
   *
   *
   * @param directory
   */
  @Override
  public void scanDirectory(File directory)
  {
    Preconditions.checkArgument(directory.isDirectory(),
      "file must be a directory");

    String basePath = directory.getAbsolutePath();

    processDirectory(basePath, directory);
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param name
   *
   * @return
   */
  private Class<?> createClass(String name)
  {
    Class<?> clazz = null;

    try
    {
      clazz = classLoader.loadClass(name);
    }
    catch (Exception ex)
    {
      logger.error("could not class ".concat(name), ex);
    }

    return clazz;
  }

  /**
   * Method description
   *
   *
   *
   * @param annotationClass
   * @param annotation
   * @param managedClass
   */
  private void processAnnotation(Class<?> annotationClass,
    Annotation annotation, Class<?> managedClass)
  {
    logger.trace("process annotation {} on class {}", annotationClass,
      managedClass);

    Collection<AnnotationProcessor> aps = processors.get(annotationClass);

    if (Util.isNotEmpty(aps))
    {
      for (AnnotationProcessor ap : aps)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("call processor {} with {} and {}", ap.getClass(),
            annotation, managedClass);
        }

        ap.processAnnotation(annotation, managedClass);
      }
    }
    else if (logger.isTraceEnabled())
    {
      logger.trace("no processor found for annotation {}",
        annotation.getClass());
    }
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param extensionObjects
   * @param packages
   * @param name
   */
  private void processClass(String name)
  {
    if (isManagedClass(packages, name))
    {
      Class<?> managedClass = createClass(name);

      if (managedClass != null)
      {
        processManagedClass(managedClass);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param extensionObjects
   * @param packages
   * @param basePath
   * @param file
   */
  private void processClassFile(String basePath, File file)
  {
    String name = file.getAbsolutePath().substring(basePath.length());

    if (name.startsWith("/"))
    {
      name = name.substring(1);
    }

    name = getClassName(name);
    processClass(name);
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param extensionObjects
   * @param directory
   * @param packages
   * @param basePath
   */
  private void processDirectory(String basePath, File directory)
  {
    File[] children = directory.listFiles();

    for (File child : children)
    {
      if (child.isDirectory())
      {
        processDirectory(basePath, child);
      }
      else if (child.getName().endsWith(".class"))
      {
        processClassFile(basePath, child);
      }
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param classLoader
   * @param extensionObjects
   * @param packages
   * @param entry
   */
  private void processEntry(JarEntry entry)
  {
    String name = entry.getName();

    if (name.endsWith(".class"))
    {
      name = getClassName(name);
      processClass(name);
    }
  }

  /**
   * Method description
   *
   *
   * @param extensionObjects
   * @param managedClass
   */
  private void processManagedClass(Class<?> managedClass)
  {
    logger.trace("check managed class {} for annotations", managedClass);

    for (Class annotationClass : processors.keySet())
    {
      Annotation annotation = managedClass.getAnnotation(annotationClass);

      if (annotation != null)
      {
        processAnnotation(annotationClass, annotation, managedClass);
      }
      else if (logger.isTraceEnabled())
      {
        logger.trace("annotation {} not found at {}", annotationClass,
          managedClass);
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  private String getClassName(String name)
  {
    return name.replaceAll("/", ".").substring(0, name.length() - 6);
  }

  /**
   * Method description
   *
   *
   * @param packages
   * @param name
   *
   * @return
   */
  private boolean isManagedClass(Collection<String> packages, String name)
  {
    boolean result = false;

    for (String pkg : packages)
    {
      if (name.startsWith(pkg))
      {
        result = true;

        break;
      }
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ClassLoader classLoader;

  /** Field description */
  private Collection<String> packages;

  /** Field description */
  private Multimap<Class, AnnotationProcessor> processors =
    HashMultimap.create();
}
