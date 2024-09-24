/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
