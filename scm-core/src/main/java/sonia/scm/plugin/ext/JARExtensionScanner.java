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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class JARExtensionScanner implements ExtensionScanner
{

  /** the logger for JARExtensionScanner */
  private static final Logger logger =
    LoggerFactory.getLogger(JARExtensionScanner.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param classLoader
   * @param extensionObjects
   * @param inputStream
   * @param packages
   *
   * @throws IOException
   */
  @Override
  public void processExtensions(ClassLoader classLoader,
                                Collection<ExtensionObject> extensionObjects,
                                InputStream inputStream,
                                Collection<String> packages)
          throws IOException
  {
    JarInputStream input = null;

    try
    {
      input = new JarInputStream(inputStream);

      JarEntry entry = input.getNextJarEntry();

      while (entry != null)
      {
        if (!entry.isDirectory())
        {
          processEntry(classLoader, extensionObjects, packages, entry);
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
   * @param classLoader
   * @param name
   *
   * @return
   */
  private Class<?> createClass(ClassLoader classLoader, String name)
  {
    Class<?> clazz = null;

    try
    {
      clazz = classLoader.loadClass(name);
    }
    catch (Exception ex)
    {
      logger.error(ex.getMessage(), ex);
    }

    return clazz;
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
  private void processEntry(ClassLoader classLoader,
                            Collection<ExtensionObject> extensionObjects,
                            Collection<String> packages, JarEntry entry)
  {
    String name = entry.getName();

    if (name.endsWith(".class"))
    {
      name = getClassName(name);

      if (isManagedClass(packages, name))
      {
        Class<?> managedClass = createClass(classLoader, name);

        if (managedClass != null)
        {
          processManagedClass(extensionObjects, managedClass);
        }
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param extensionObjects
   * @param managedClass
   */
  private void processManagedClass(
          Collection<ExtensionObject> extensionObjects, Class<?> managedClass)
  {
    Extension extension = managedClass.getAnnotation(Extension.class);

    if (extension != null)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("found extension class {}", managedClass.getName());
      }

      extensionObjects.add(new ExtensionObject(extension, managedClass));
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
}
