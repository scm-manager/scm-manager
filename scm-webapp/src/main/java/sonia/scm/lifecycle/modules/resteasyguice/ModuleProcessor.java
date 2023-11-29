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

import com.google.inject.Binding;
import com.google.inject.Injector;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.GetRestful;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ModuleProcessor
{

   private final Registry registry;
   private final ResteasyProviderFactory providerFactory;

   public ModuleProcessor(final Registry registry, final ResteasyProviderFactory providerFactory)
   {
      this.registry = registry;
      this.providerFactory = providerFactory;
   }

   public void processInjector(final Injector injector)
   {
      List<Binding<?>> rootResourceBindings = new ArrayList<Binding<?>>();
      for (final Binding<?> binding : injector.getBindings().values())
      {
         final Type type = binding.getKey().getTypeLiteral().getRawType();
         if (type instanceof Class)
         {
            final Class<?> beanClass = (Class) type;
            if (GetRestful.isRootResource(beanClass))
            {
               // deferred registration
               rootResourceBindings.add(binding);
            }
            if (beanClass.isAnnotationPresent(Provider.class))
            {
               providerFactory.registerProviderInstance(binding.getProvider().get());
            }
         }
      }
      for (Binding<?> binding : rootResourceBindings)
      {
         Class<?> beanClass = (Class) binding.getKey().getTypeLiteral().getType();
         final ResourceFactory resourceFactory = new GuiceResourceFactory(binding.getProvider(), beanClass);
         registry.addResourceFactory(resourceFactory);
      }
   }
}
