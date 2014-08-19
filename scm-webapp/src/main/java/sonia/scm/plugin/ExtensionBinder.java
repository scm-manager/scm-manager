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

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.EagerSingleton;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

import javax.ws.rs.ext.Provider;

/**
 *
 * @author Sebastian Sdorra
 */
@SuppressWarnings("unchecked")
public final class ExtensionBinder
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
   * @param mutipleExtensionPoints
   * @param singleExtensionPoints
   * @param extensions
   */
  public void bind(Set<Class> mutipleExtensionPoints,
    Set<Class> singleExtensionPoints, Set<Class> extensions)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("bind {} extensions to {} extension points",
        extensions.size(),
        mutipleExtensionPoints.size() + singleExtensionPoints.size());
    }

    Set<Class> boundClasses = Sets.newHashSet();

    for (Class extensionPointClass : mutipleExtensionPoints)
    {
      bindMultiExtensionPoint(boundClasses, extensionPointClass, extensions);
    }

    for (Class extensionPointClass : singleExtensionPoints)
    {
      bindExtensionPoint(boundClasses, extensionPointClass, extensions);
    }

    Set<Class> extensionsCopy = Sets.newHashSet(extensions);

    Iterables.removeAll(extensionsCopy, boundClasses);

    for (Class extension : extensionsCopy)
    {
      AnnotatedBindingBuilder abb = binder.bind(extension);

      if (isProvider(extension))
      {
        logger.info("bind provider {} as singleton", extension);
        abb.in(Scopes.SINGLETON);
      }
      else if (isEagerSingleton(extension))
      {

        logger.info("bind {} as eager singleton, without extensionpoint",
          extension);
        abb.asEagerSingleton();
      }
      else
      {
        logger.info("bind {}, without extensionpoint", extension);
        binder.bind(extension);
      }
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param found
   *
   * @param boundClasses
   * @param extensionPointClass
   * @param extensions
   */
  @SuppressWarnings("unchecked")
  private void bindExtensionPoint(Set<Class> boundClasses,
    Class extensionPointClass, Set<Class> extensions)
  {
    boolean bound = false;

    for (Class extensionClass : extensions)
    {
      if (extensionPointClass.isAssignableFrom(extensionClass))
      {
        if (bound)
        {
          throw new IllegalStateException(
            "extension point ".concat(extensionPointClass.getName()).concat(
              " is not multiple and is already bound to another class"));
        }

        bindSingleInstance(extensionPointClass, extensionClass);
        boundClasses.add(extensionClass);
        bound = true;
      }
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param found
   *
   * @param boundClasses
   * @param extensionPointClass
   * @param extensions
   */
  @SuppressWarnings("unchecked")
  private void bindMultiExtensionPoint(Set<Class> boundClasses,
    Class extensionPointClass, Iterable<Class> extensions)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("create multibinder for {}", extensionPointClass.getName());
    }

    Multibinder multibinder = Multibinder.newSetBinder(binder,
                                extensionPointClass);

    for (Class extensionClass : extensions)
    {
      if (extensionPointClass.isAssignableFrom(extensionClass))
      {
        boolean eagerSingleton = isEagerSingleton(extensionClass);

        if (logger.isInfoEnabled())
        {
          String as = Util.EMPTY_STRING;

          if (eagerSingleton)
          {
            as = " as eager singleton";
          }

          logger.info("bind {} to multibinder of {}{}",
            extensionClass.getName(), extensionPointClass.getName(), as);
        }

        ScopedBindingBuilder sbb = multibinder.addBinding().to(extensionClass);

        if (eagerSingleton)
        {
          sbb.asEagerSingleton();
          logger.info("bind {} as eager singleton");
        }

        boundClasses.add(extensionClass);
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
  @SuppressWarnings("unchecked")
  private void bindSingleInstance(Class extensionPointClass,
    Class extensionClass)
  {
    boolean eagerSingleton = isEagerSingleton(extensionClass);

    if (logger.isInfoEnabled())
    {
      String as = Util.EMPTY_STRING;

      if (eagerSingleton)
      {
        as = " as eager singleton";
      }

      logger.info("bind {} to {}{}", extensionClass.getName(),
        extensionPointClass.getName(), as);
    }

    ScopedBindingBuilder sbb =
      binder.bind(extensionPointClass).to(extensionClass);

    if (eagerSingleton)
    {
      sbb.asEagerSingleton();
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param extensionClass
   *
   * @return
   */
  private boolean isEagerSingleton(Class extensionClass)
  {
    return extensionClass.isAnnotationPresent(EagerSingleton.class);
  }

  /**
   * Method description
   *
   *
   * @param extensionClass
   *
   * @return
   */
  private boolean isProvider(Class extensionClass)
  {
    return extensionClass.isAnnotationPresent(Provider.class);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Binder binder;
}
