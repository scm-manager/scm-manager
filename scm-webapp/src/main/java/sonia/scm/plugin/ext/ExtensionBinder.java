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

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ExtensionPoint;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class ExtensionBinder
{

  /**
   * the logger for ExtensionBinder
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ExtensionBinder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param binder
   */
  public ExtensionBinder(Binder binder)
  {
    this.binder = binder;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param bounds
   * @param extensionPoints
   * @param extensions
   */
  public void bind(Set<AnnotatedClass<Extension>> bounds,
    Set<AnnotatedClass<ExtensionPoint>> extensionPoints,
    Set<AnnotatedClass<Extension>> extensions)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("bind {} extensions to {} extension points",
        extensions.size(), extensionPoints.size());
    }

    for (AnnotatedClass<ExtensionPoint> extensionPoint : extensionPoints)
    {
      ExtensionPoint extensionPointAnnotation = extensionPoint.getAnnotation();
      Class extensionPointClass = extensionPoint.getAnnotatedClass();

      if (extensionPointAnnotation.multi())
      {
        bindMultiExtensionPoint(bounds, extensionPointClass, extensions);
      }
      else
      {
        bindExtensionPoint(bounds, extensionPointClass, extensions);
      }
    }

    Set<AnnotatedClass<Extension>> extensionsCopy = Sets.newHashSet(extensions);

    Iterables.removeAll(extensionsCopy, bounds);

    for (AnnotatedClass<Extension> extension : extensionsCopy)
    {
      logger.info("bind {}, without extensionpoint",
        extension.getAnnotatedClass());
      binder.bind(extension.getAnnotatedClass());
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param found
   * @param extensionPointClass
   * @param extensions
   */
  private void bindExtensionPoint(Set<AnnotatedClass<Extension>> found,
    Class extensionPointClass, Set<AnnotatedClass<Extension>> extensions)
  {
    for (AnnotatedClass<Extension> extension : extensions)
    {
      Class extensionClass = extension.getAnnotatedClass();

      if (extensionPointClass.isAssignableFrom(extensionClass))
      {
        found.add(extension);
        bindSingleInstance(extensionPointClass, extensionClass);

        break;
      }
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param found
   * @param extensionPointClass
   * @param extensions
   */
  private void bindMultiExtensionPoint(Set<AnnotatedClass<Extension>> found,
    Class extensionPointClass, Set<AnnotatedClass<Extension>> extensions)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("create multibinder for {}", extensionPointClass.getName());
    }

    Multibinder multibinder = Multibinder.newSetBinder(binder,
                                extensionPointClass);

    for (AnnotatedClass<Extension> extension : extensions)
    {
      Class extensionClass = extension.getAnnotatedClass();

      if (extensionPointClass.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind {} to multibinder of {}", extensionClass.getName(),
            extensionPointClass.getName());
        }

        found.add(extension);
        multibinder.addBinding().to(extensionClass);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param extensionPointClass
   * @param extensionClass
   */
  private void bindSingleInstance(Class extensionPointClass,
    Class extensionClass)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("bind {} to {}", extensionClass.getName(),
        extensionPointClass.getName());
    }

    binder.bind(extensionPointClass).to(extensionClass);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Binder binder;
}
