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
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

//~--- JDK imports ------------------------------------------------------------

import java.lang.annotation.Annotation;

/**
 *
 * @author Sebastian Sdorra
 */
public class EagerSingletonScopeModule extends AbstractModule
{

  /** Field description */
  private static EagerSingletonScope EAGERSINGLETON_SCOPE =
    new EagerSingletonScope();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void configure()
  {
    bindScope(EagerSingleton.class, EAGERSINGLETON_SCOPE);

    TypeListener listener = new EagerCreatingListener();

    requestInjection(listener);
    bindListener(Matchers.any(), listener);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/12/07
   * @author         Enter your name here...
   */
  private static class EagerCreatingListener implements TypeListener
  {

    /**
     * Method description
     *
     *
     * @param type
     * @param encounter
     * @param <I>
     */
    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter)
    {
      if (injector != null)
      {
        createIfEager(injector.getBinding(Key.get(type)));
      }
    }

    /**
     * Method description
     *
     *
     * @param injector
     */
    @Inject
    void injector(Injector injector)
    {
      this.injector = injector;

      for (Binding<?> b : injector.getBindings().values())
      {
        createIfEager(b);
      }
    }

    /**
     * Method description
     *
     *
     * @param b
     */
    private void createIfEager(final Binding<?> b)
    {
      b.acceptScopingVisitor(new BindingScopingVisitor<Void>()
      {
        @Override
        public Void visitEagerSingleton()
        {
          return null;
        }

        @Override
        public Void visitNoScoping()
        {
          return null;
        }

        @Override
        public Void visitScope(Scope scope)
        {
          if (scope == EAGERSINGLETON_SCOPE)
          {
            b.getProvider().get();
          }

          return null;
        }

        @Override
        public Void visitScopeAnnotation(
          Class<? extends Annotation> scopeAnnotation)
        {
          return null;
        }
      });
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private Injector injector;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/12/07
   * @author         Enter your name here...
   */
  private static class EagerSingletonScope implements Scope
  {

    /**
     * Method description
     *
     *
     * @param key
     * @param unscoped
     * @param <T>
     *
     * @return
     */
    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped)
    {
      return Scopes.SINGLETON.scope(key, unscoped);
    }
  }
}
