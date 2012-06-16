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



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.group.GroupListener;
import sonia.scm.io.FileSystem;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.plugin.ext.ExtensionProcessor;
import sonia.scm.repository.ChangesetPreProcessor;
import sonia.scm.repository.ChangesetPreProcessorFactory;
import sonia.scm.repository.FileObjectPreProcessor;
import sonia.scm.repository.FileObjectPreProcessorFactory;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryHook;
import sonia.scm.repository.RepositoryListener;
import sonia.scm.repository.RepositoryRequestListener;
import sonia.scm.repository.spi.RepositoryServiceResolver;
import sonia.scm.resources.ResourceHandler;
import sonia.scm.security.EncryptionHandler;
import sonia.scm.user.UserListener;
import sonia.scm.web.security.AuthenticationHandler;
import sonia.scm.web.security.AuthenticationListener;
import sonia.scm.web.security.DefaultAuthenticationHandler;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContextListener;

/**
 *
 * @author Sebastian Sdorra
 */
public class BindingExtensionProcessor implements ExtensionProcessor
{

  /** the logger for BindingExtensionProcessor */
  private static final Logger logger =
    LoggerFactory.getLogger(BindingExtensionProcessor.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   */
  public BindingExtensionProcessor()
  {
    this.moduleSet = new HashSet<Module>();
    this.extensions = new HashSet<Class<?>>();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param binder
   */
  @SuppressWarnings("unchecked")
  public void bindExtensions(Binder binder)
  {
    Multibinder<RepositoryHandler> repositoryHandlers =
      Multibinder.newSetBinder(binder, RepositoryHandler.class);
    Multibinder<AuthenticationHandler> authenticators =
      Multibinder.newSetBinder(binder, AuthenticationHandler.class);
    Multibinder<ResourceHandler> resourceHandler =
      Multibinder.newSetBinder(binder, ResourceHandler.class);
    Multibinder<RepositoryHook> repositoryHookBinder =
      Multibinder.newSetBinder(binder, RepositoryHook.class);

    // changeset pre processor
    Multibinder<ChangesetPreProcessor> changesetPreProcessorBinder =
      Multibinder.newSetBinder(binder, ChangesetPreProcessor.class);
    Multibinder<ChangesetPreProcessorFactory> changesetPreProcessorFactoryBinder =
      Multibinder.newSetBinder(binder, ChangesetPreProcessorFactory.class);

    // fileobject pre processor
    Multibinder<FileObjectPreProcessor> fileObjectPreProcessorBinder =
      Multibinder.newSetBinder(binder, FileObjectPreProcessor.class);
    Multibinder<FileObjectPreProcessorFactory> fileObjectPreProcessorFactoryBinder =
      Multibinder.newSetBinder(binder, FileObjectPreProcessorFactory.class);

    // repository service resolver
    Multibinder<RepositoryServiceResolver> repositoryServiceResolverBinder =
      Multibinder.newSetBinder(binder, RepositoryServiceResolver.class);

    // listeners
    Multibinder<RepositoryListener> repositoryListenerBinder =
      Multibinder.newSetBinder(binder, RepositoryListener.class);
    Multibinder<UserListener> userListenerBinder =
      Multibinder.newSetBinder(binder, UserListener.class);
    Multibinder<GroupListener> groupListenerBinder =
      Multibinder.newSetBinder(binder, GroupListener.class);
    Multibinder<AuthenticationListener> authenticationListenerBinder =
      Multibinder.newSetBinder(binder, AuthenticationListener.class);
    Multibinder<RepositoryRequestListener> repositoryRequestListenerBinder =
      Multibinder.newSetBinder(binder, RepositoryRequestListener.class);
    Multibinder<ServletContextListener> servletContextListenerBinder =
      Multibinder.newSetBinder(binder, ServletContextListener.class);

    authenticators.addBinding().to(DefaultAuthenticationHandler.class);

    for (Class extensionClass : extensions)
    {
      if (RepositoryHandler.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind RepositoryHandler {}", extensionClass.getName());
        }

        binder.bind(extensionClass);
        repositoryHandlers.addBinding().to(extensionClass);
      }
      else if (EncryptionHandler.class.isAssignableFrom(extensionClass))
      {
        bind(binder, EncryptionHandler.class, extensionClass);
      }
      else if (AuthenticationHandler.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind AuthenticationHandler {}",
                      extensionClass.getName());
        }

        binder.bind(extensionClass);
        authenticators.addBinding().to(extensionClass);
      }
      else if (GroupListener.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind GroupListener {}", extensionClass.getName());
        }

        binder.bind(extensionClass);
        groupListenerBinder.addBinding().to(extensionClass);
      }
      else if (UserListener.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind UserListener {}", extensionClass.getName());
        }

        binder.bind(extensionClass);
        userListenerBinder.addBinding().to(extensionClass);
      }
      else if (RepositoryListener.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind RepositoryListener {}", extensionClass.getName());
        }

        binder.bind(extensionClass);
        repositoryListenerBinder.addBinding().to(extensionClass);
      }
      else if (AuthenticationListener.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind AuthenticaitonListener {}",
                      extensionClass.getName());
        }

        binder.bind(extensionClass);
        authenticationListenerBinder.addBinding().to(extensionClass);
      }
      else if (ResourceHandler.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind ResourceHandler {}", extensionClass.getName());
        }

        resourceHandler.addBinding().to(extensionClass);
      }
      else if (FileSystem.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind FileSystem {}", extensionClass.getName());
        }

        fileSystemClass = extensionClass;
      }
      else if (ChangesetPreProcessor.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind ChangesetPreProcessor {}",
                      extensionClass.getName());
        }

        changesetPreProcessorBinder.addBinding().to(extensionClass);
      }
      else if (ChangesetPreProcessorFactory.class.isAssignableFrom(
              extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind ChangesetPreProcessorFactory {}",
                      extensionClass.getName());
        }

        changesetPreProcessorFactoryBinder.addBinding().to(extensionClass);
      }
      else if (FileObjectPreProcessor.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind FileObjectPreProcessor {}",
                      extensionClass.getName());
        }

        fileObjectPreProcessorBinder.addBinding().to(extensionClass);
      }
      else if (FileObjectPreProcessorFactory.class.isAssignableFrom(
              extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind FileObjectPreProcessorFactory {}",
                      extensionClass.getName());
        }

        fileObjectPreProcessorFactoryBinder.addBinding().to(extensionClass);
      }
      else if (RepositoryHook.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind RepositoryHook {}", extensionClass.getName());
        }

        repositoryHookBinder.addBinding().to(extensionClass);
      }
      else if (RepositoryRequestListener.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind RepositoryRequestListener {}",
                      extensionClass.getName());
        }

        repositoryRequestListenerBinder.addBinding().to(extensionClass);
      }
      else if (ServletContextListener.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind ServletContextListener {}",
                      extensionClass.getName());
        }

        servletContextListenerBinder.addBinding().to(extensionClass);
      }
      else if (RepositoryServiceResolver.class.isAssignableFrom(extensionClass))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind RepositoryServiceResolver {}",
                      extensionClass.getName());
        }

        repositoryServiceResolverBinder.addBinding().to(extensionClass);
      }
      else
      {
        if (logger.isInfoEnabled())
        {
          logger.info("bind {}", extensionClass.getName());
        }

        binder.bind(extensionClass);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param extension
   * @param extensionClass
   */
  @Override
  public void processExtension(Extension extension, Class extensionClass)
  {
    if (Module.class.isAssignableFrom(extensionClass))
    {
      if (logger.isInfoEnabled())
      {
        logger.info("add GuiceModule {}", extensionClass.getName());
      }

      addModuleClass(extensionClass);
    }
    else
    {
      extensions.add(extensionClass);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Class<? extends FileSystem> getFileSystemClass()
  {
    return fileSystemClass;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<RepositoryHook> getHooks()
  {
    return hooks;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<Module> getModuleSet()
  {
    return moduleSet;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param extensionClass
   */
  private void addModuleClass(Class<? extends Module> extensionClass)
  {
    try
    {
      Module module = extensionClass.newInstance();

      moduleSet.add(module);
    }
    catch (Exception ex)
    {
      logger.error(ex.getMessage(), ex);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param binder
   * @param type
   * @param bindingType
   * @param <T>
   *
   */
  private <T> void bind(Binder binder, Class<T> type,
                        Class<? extends T> bindingType)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("bind {} of type {}", type.getName(), bindingType.getName());
    }

    binder.bind(type).to(bindingType);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<Class<?>> extensions;

  /** Field description */
  private Class<? extends FileSystem> fileSystemClass;

  /** Field description */
  private Set<RepositoryHook> hooks = new HashSet<RepositoryHook>();

  /** Field description */
  private Set<Module> moduleSet;
}
