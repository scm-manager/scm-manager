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
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryListener;
import sonia.scm.resources.ResourceHandler;
import sonia.scm.security.EncryptionHandler;
import sonia.scm.user.UserListener;
import sonia.scm.web.security.AuthenticationHandler;
import sonia.scm.web.security.AuthenticationListener;
import sonia.scm.web.security.XmlAuthenticationHandler;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashSet;
import java.util.Set;

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

    authenticators.addBinding().to(XmlAuthenticationHandler.class);

    for (Class extensionClass : extensions)
    {
      try
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

          GroupListener listener = (GroupListener) extensionClass.newInstance();

          groupListeners.add(listener);
        }
        else if (UserListener.class.isAssignableFrom(extensionClass))
        {
          if (logger.isInfoEnabled())
          {
            logger.info("bind UserListener {}", extensionClass.getName());
          }

          UserListener listener = (UserListener) extensionClass.newInstance();

          userListeners.add(listener);
        }
        else if (RepositoryListener.class.isAssignableFrom(extensionClass))
        {
          if (logger.isInfoEnabled())
          {
            logger.info("bind RepositoryListener {}", extensionClass.getName());
          }

          RepositoryListener listener =
            (RepositoryListener) extensionClass.newInstance();

          repositoryListeners.add(listener);
        }
        else if (AuthenticationListener.class.isAssignableFrom(extensionClass))
        {
          if (logger.isInfoEnabled())
          {
            logger.info("bind AuthenticaitonListener {}",
                        extensionClass.getName());
          }

          AuthenticationListener listener =
            (AuthenticationListener) extensionClass.newInstance();

          authenticationListeners.add(listener);
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
        else
        {
          if (logger.isInfoEnabled())
          {
            logger.info("bind {}", extensionClass.getName());
          }

          binder.bind(extensionClass);
        }
      }
      catch (IllegalAccessException ex)
      {
        logger.error(ex.getMessage(), ex);
      }
      catch (InstantiationException ex)
      {
        logger.error(ex.getMessage(), ex);
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
  public Set<AuthenticationListener> getAuthenticationListeners()
  {
    return authenticationListeners;
  }

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
  public Set<GroupListener> getGroupListeners()
  {
    return groupListeners;
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

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<RepositoryListener> getRepositoryListeners()
  {
    return repositoryListeners;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<UserListener> getUserListeners()
  {
    return userListeners;
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
   * @return
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
  private Set<Module> moduleSet;

  /** Field description */
  private Set<RepositoryListener> repositoryListeners =
    new HashSet<RepositoryListener>();

  /** Field description */
  private Set<AuthenticationListener> authenticationListeners =
    new HashSet<AuthenticationListener>();

  /** Field description */
  private Set<UserListener> userListeners = new HashSet<UserListener>();

  /** Field description */
  private Set<GroupListener> groupListeners = new HashSet<GroupListener>();
}
