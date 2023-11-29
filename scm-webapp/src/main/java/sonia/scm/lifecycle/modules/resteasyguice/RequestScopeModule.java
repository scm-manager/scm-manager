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

package sonia.scm.lifecycle.modules.resteasyguice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.core.ResteasyContext;

/**
 * Binds the {@link RequestScoped} to the current HTTP request and
 * makes all the classes available via the {@link javax.ws.rs.core.Context} annotation injectable.
 */
public class RequestScopeModule extends AbstractModule
{
   @Override
   protected void configure()
   {
      bindScope(RequestScoped.class, new Scope()
      {
         @Override
         public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator)
         {
            return new Provider<T>()
            {
               @SuppressWarnings("unchecked")
               @Override
               public T get()
               {
                  Class<T> instanceClass = (Class<T>) key.getTypeLiteral().getType();
                  T instance = ResteasyContext.getContextData(instanceClass);

                  if (instance == null) {
                     instance = creator.get();
                     ResteasyContext.pushContext(instanceClass, instance);
                  }

                  return instance;
               }

               @Override
               public String toString() {
                  return String.format("%s[%s]", creator, super.toString());
               }
            };
         }
      });

      bind(HttpServletRequest.class).toProvider(new ResteasyContextProvider<HttpServletRequest>(HttpServletRequest.class)).in(RequestScoped.class);
      bind(HttpServletResponse.class).toProvider(new ResteasyContextProvider<HttpServletResponse>(HttpServletResponse.class)).in(RequestScoped.class);
      bind(Request.class).toProvider(new ResteasyContextProvider<Request>(Request.class)).in(RequestScoped.class);
      bind(HttpHeaders.class).toProvider(new ResteasyContextProvider<HttpHeaders>(HttpHeaders.class)).in(RequestScoped.class);
      bind(UriInfo.class).toProvider(new ResteasyContextProvider<UriInfo>(UriInfo.class)).in(RequestScoped.class);
      bind(SecurityContext.class).toProvider(new ResteasyContextProvider<SecurityContext>(SecurityContext.class)).in(RequestScoped.class);
      bind(ServletConfig.class).toProvider(new ResteasyContextProvider<ServletConfig>(ServletConfig.class)).in(Singleton.class);
      bind(ServletContext.class).toProvider(new ResteasyContextProvider<ServletContext>(ServletContext.class)).in(Singleton.class);
   }

   private static class ResteasyContextProvider<T> implements Provider<T> {

      private final Class<T> instanceClass;

      ResteasyContextProvider(final Class<T> instanceClass)
      {
         this.instanceClass = instanceClass;
      }

      @Override
      public T get() {
         return ResteasyContext.getContextData(instanceClass);
      }
   }
}
