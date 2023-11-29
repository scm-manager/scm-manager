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

import com.google.inject.Provider;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.PropertyInjector;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import java.util.concurrent.CompletionStage;

public class GuiceResourceFactory implements ResourceFactory
{

   private final Provider provider;
   private final Class<?> scannableClass;
   private PropertyInjector propertyInjector;

   public GuiceResourceFactory(final Provider provider, final Class<?> scannableClass)
   {
      this.provider = provider;
      this.scannableClass = scannableClass;
   }

   public Class<?> getScannableClass()
   {
      return scannableClass;
   }

   public void registered(ResteasyProviderFactory factory)
   {
      propertyInjector = factory.getInjectorFactory().createPropertyInjector(scannableClass, factory);
   }

   @Override
   public Object createResource(final HttpRequest request, final HttpResponse response, final ResteasyProviderFactory factory)
   {
      final Object resource = provider.get();
      CompletionStage<Void> propertyStage = propertyInjector.inject(request, response, resource, true);
      return propertyStage == null ? resource : propertyStage
            .thenApply(v -> resource);
   }

   public void requestFinished(final HttpRequest request, final HttpResponse response, final Object resource)
   {
   }

   public void unregistered()
   {
   }
}
