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



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.HgRepositoryHookEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryNotFoundException;

//~--- JDK imports ------------------------------------------------------------

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("hook/hg/{repository}/{type}")
public class HgHookCallback
{

  /** the logger for HgHookCallback */
  private static final Logger logger =
    LoggerFactory.getLogger(HgHookCallback.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   * @param handler
   * @param hookManager
   */
  @Inject
  public HgHookCallback(RepositoryManager repositoryManager,
                        HgRepositoryHandler handler, HgHookManager hookManager)
  {
    this.repositoryManager = repositoryManager;
    this.handler = handler;
    this.hookManager = hookManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * TODO: protect
   *
   *
   *
   * @param repositoryName
   * @param type
   * @param challenge
   * @param node
   *
   * @return
   */
  @POST
  public Response hookCallback(@PathParam("repository") String repositoryName,
                               @PathParam("type") String type,
                               @FormParam("challenge") String challenge,
                               @FormParam("node") String node)
  {
    Response response = null;

    if (hookManager.isAcceptAble(challenge))
    {
      try
      {
        repositoryManager.fireHookEvent(HgRepositoryHandler.TYPE_NAME,
                                        repositoryName,
                                        new HgRepositoryHookEvent(handler,
                                          repositoryName, node));
        response = Response.ok().build();
      }
      catch (RepositoryNotFoundException ex)
      {
        if (logger.isErrorEnabled())
        {
          logger.error("could not find repository {}", repositoryName);

          if (logger.isTraceEnabled())
          {
            logger.trace("repository not found", ex);
          }
        }

        response = Response.status(Response.Status.NOT_FOUND).build();
      }
    }
    else
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("hg hook challenge is not accept able");
      }

      response = Response.status(Response.Status.BAD_REQUEST).build();
    }

    return response;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private HgHookManager hookManager;

  /** Field description */
  private RepositoryManager repositoryManager;
}
