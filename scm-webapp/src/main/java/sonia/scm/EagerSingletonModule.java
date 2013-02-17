/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.lang.annotation.Annotation;

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class EagerSingletonModule extends AbstractModule
{

  /**
   * the logger for EagerSingletonModule
   */
  private static final Logger logger =
    LoggerFactory.getLogger(EagerSingletonModule.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param injector
   */
  void initialize(Injector injector)
  {
    for (Class<?> clazz : eagerSingletons)
    {
      logger.info("initialize eager singleton {}", clazz.getName());
      injector.getInstance(clazz);
    }
  }

  /**
   * Method description
   *
   */
  @Override
  protected void configure()
  {
    bindListener(isAnnotatedWith(EagerSingleton.class), new TypeListener()
    {

      @Override
      public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter)
      {
        eagerSingletons.add(type.getRawType());
      }
    });

    bind(EagerSingletonModule.class).toInstance(this);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param annotation
   *
   * @return
   */
  private Matcher<TypeLiteral<?>> isAnnotatedWith(
    final Class<? extends Annotation> annotation)
  {
    return new AbstractMatcher<TypeLiteral<?>>()
    {
      @Override
      public boolean matches(TypeLiteral<?> type)
      {
        return type.getRawType().isAnnotationPresent(annotation);
      }
    };
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Set<Class<?>> eagerSingletons = Sets.newHashSet();
}
