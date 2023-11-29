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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class GuiceResteasyBootstrapServletContextListener extends ResteasyBootstrap implements ServletContextListener
{

   private List<? extends Module> modules;
   @Inject private Injector parentInjector;

   @Override
   public void contextInitialized(final ServletContextEvent event)
   {
      super.contextInitialized(event);
      final ServletContext context = event.getServletContext();
      final ResteasyDeployment deployment = (ResteasyDeployment) context.getAttribute(ResteasyDeployment.class.getName());
      final Registry registry = deployment.getRegistry();
      final ResteasyProviderFactory providerFactory = deployment.getProviderFactory();
      final ModuleProcessor processor = new ModuleProcessor(registry, providerFactory);
      final List<? extends Module> modules = getModules(context);
      final Stage stage = getStage(context);
      Injector injector;

      if (parentInjector != null) {
         injector = parentInjector.createChildInjector(modules);
      } else {
         if (stage == null)
         {
            injector = Guice.createInjector(modules);
         }
         else
         {
            injector = Guice.createInjector(stage, modules);
         }
      }
      withInjector(injector);
      processor.processInjector(injector);

      //load parent injectors
      while (injector.getParent() != null) {
         injector = injector.getParent();
         processor.processInjector(injector);
      }
      this.modules = modules;
      triggerAnnotatedMethods(PostConstruct.class);
   }

   /**
    * Override this method to interact with the {@link com.google.inject.Injector} after it has been created. The default is no-op.
    *
    * @param injector
    */
   protected void withInjector(Injector injector)
   {
   }

   /**
    * Override this method to set the Stage. By default it is taken from resteasy.guice.stage context param.
    *
    * @param context
    * @return Guice Stage
    */
   protected Stage getStage(ServletContext context)
   {
      final String stageAsString = context.getInitParameter("resteasy.guice.stage");
      if (stageAsString == null)
      {
         return null;
      }
      try
      {
         return Stage.valueOf(stageAsString.trim());
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(stageAsString);
      }
   }

   /**
    * Override this method to instantiate your {@link com.google.inject.Module}s yourself.
    *
    * @param context
    * @return
    */
   protected List<? extends Module> getModules(final ServletContext context)
   {
      final List<Module> result = new ArrayList<Module>();
      final String modulesString = context.getInitParameter("resteasy.guice.modules");
      if (modulesString != null)
      {
         final String[] moduleStrings = modulesString.trim().split(",");
         for (final String moduleString : moduleStrings)
         {
            try
            {
               final Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(moduleString.trim());
               final Module module = (Module) clazz.newInstance();
               result.add(module);
            }
            catch (ClassNotFoundException e)
            {
               throw new RuntimeException(e);
            }
            catch (IllegalAccessException e)
            {
               throw new RuntimeException(e);
            }
            catch (InstantiationException e)
            {
               throw new RuntimeException(e);
            }
         }

      }
      return result;
   }

   @Override
   public void contextDestroyed(final ServletContextEvent event)
   {
      triggerAnnotatedMethods(PreDestroy.class);
   }

   private void triggerAnnotatedMethods(final Class<? extends Annotation> annotationClass)
   {
      for (Module module : this.modules)
      {
         final Method[] methods = module.getClass().getMethods();
         for (Method method : methods)
         {
            if (method.isAnnotationPresent(annotationClass))
            {
               if(method.getParameterTypes().length > 0)
               {
                  continue;
               }
               try
               {
                  method.invoke(module);
               } catch (InvocationTargetException ex) {
               } catch (IllegalAccessException ex) {
               }
            }
         }
      }
   }
}
