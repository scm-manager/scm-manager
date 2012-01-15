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



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.codehaus.enunciate.modules.jersey.SpringManagedLifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.NotSupportedFeatuerException;
import sonia.scm.Type;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.util.SecurityUtil;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("import/repositories")
@SpringManagedLifecycle
public class RepositoryImportResource
{

  /**
   * the logger for RepositoryImportResource
   */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryImportResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param manager
   * @param securityContextProvider
   */
  @Inject
  public RepositoryImportResource(
          RepositoryManager manager,
          Provider<WebSecurityContext> securityContextProvider)
  {
    this.manager = manager;
    this.securityContextProvider = securityContextProvider;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public GenericEntity<List<Type>> getImportableTypes()
  {
    SecurityUtil.assertIsAdmin(securityContextProvider);

    List<Type> types = new ArrayList<Type>();
    Collection<Type> handlerTypes = manager.getTypes();

    for (Type t : handlerTypes)
    {
      RepositoryHandler handler = manager.getHandler(t.getName());

      if (handler != null)
      {
        try
        {
          if (handler.getImportHandler() != null)
          {
            types.add(t);
          }
        }
        catch (NotSupportedFeatuerException ex)
        {
          if (logger.isTraceEnabled())
          {
            logger.trace("import handler is not supported", ex);
          }
          else if (logger.isInfoEnabled())
          {
            logger.info("{} handler does not support import of repositories",
                        t.getName());
          }
        }
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find handler for type {}", t.getName());
      }
    }

    return new GenericEntity<List<Type>>(types) {}
    ;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private RepositoryManager manager;

  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;
}
