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

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.event.ScmEventBus;
import sonia.scm.event.Subscriber;

//~--- JDK imports ------------------------------------------------------------

import java.lang.annotation.Annotation;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmSubscriberModule extends AbstractModule
{

  /**
   * the logger for ScmSubscriberModule
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ScmSubscriberModule.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void configure()
  {
    bindListener(annotatedWith(Subscriber.class), new TypeListener()
    {

      @Override
      public <I> void hear(TypeLiteral<I> type,
        final TypeEncounter<I> encounter)
      {
        encounter.register(new InjectionListener<I>()
        {
          @Override
          public void afterInjection(Object object)
          {
            if (logger.isTraceEnabled())
            {
              logger.trace("register subscriber {}", object.getClass());
            }

            Subscriber subscriber =
              object.getClass().getAnnotation(Subscriber.class);
            

            ScmEventBus.getInstance().register(object, subscriber.async());
          }
        });
      }

    });
  }

  /**
   * Method description
   *
   *
   * @param aClass
   *
   * @return
   */
  private Matcher<TypeLiteral<?>> annotatedWith(
    Class<? extends Annotation> aClass)
  {
    return new AnnotatedWith(aClass);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/12/07
   * @author         Enter your name here...
   */
  private static class AnnotatedWith extends AbstractMatcher<TypeLiteral<?>>
  {

    /**
     * Constructs ...
     *
     *
     * @param baseClass
     */
    private AnnotatedWith(Class<? extends Annotation> baseClass)
    {
      this.baseClass = baseClass;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param t
     *
     * @return
     */
    @Override
    public boolean matches(TypeLiteral<?> t)
    {
      try
      {
        return t.getRawType().isAnnotationPresent(baseClass);
      }
      catch (Exception e)
      {

        // LOG e
        return false;
      }
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Class<? extends Annotation> baseClass;
  }
}
