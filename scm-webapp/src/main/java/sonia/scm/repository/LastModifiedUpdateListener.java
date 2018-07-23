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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 * @since 1.37
 */
@Extension
@EagerSingleton
public final class LastModifiedUpdateListener
{

  /**
   * the logger for LastModifiedUpdateListener
   */
  private static final Logger logger =
    LoggerFactory.getLogger(LastModifiedUpdateListener.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param adminContext
   * @param repositoryManager
   */
  @Inject
  public LastModifiedUpdateListener(AdministrationContext adminContext,
    RepositoryManager repositoryManager)
  {
    this.adminContext = adminContext;
    this.repositoryManager = repositoryManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   */
  @Subscribe
  public void onPostReceive(PostReceiveRepositoryHookEvent event)
  {
    final Repository repository = event.getRepository();

    if (repository != null)
    {
      //J-
      adminContext.runAsAdmin(
        new LastModifiedPrivilegedAction(repositoryManager, repository)
      );
      //J+
    }
    else
    {
      logger.warn("recevied hook without repository");
    }
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/04/20
   * @author         Enter your name here...
   */
  static class LastModifiedPrivilegedAction implements PrivilegedAction
  {

    /**
     * Constructs ...
     *
     *
     * @param repositoryManager
     * @param repository
     */
    public LastModifiedPrivilegedAction(RepositoryManager repositoryManager,
      Repository repository)
    {
      this.repositoryManager = repositoryManager;
      this.repository = repository;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     */
    @Override
    public void run()
    {
      Repository dbr = repositoryManager.get(repository.getId());

      if (dbr != null)
      {
        logger.info("update last modified date of repository {}", dbr.getId());
        dbr.setLastModified(System.currentTimeMillis());

        try
        {
          repositoryManager.modify(dbr);
        }
        catch (RepositoryException ex)
        {
          logger.error("could not modify repository", ex);
        }
      }
      else
      {
        logger.error("could not find repository with id {}",
          repository.getId());
      }
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Repository repository;

    /** Field description */
    private final RepositoryManager repositoryManager;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final AdministrationContext adminContext;

  /** Field description */
  private final RepositoryManager repositoryManager;
}
