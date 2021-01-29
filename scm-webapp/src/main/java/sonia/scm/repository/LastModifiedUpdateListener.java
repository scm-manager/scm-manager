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

package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.NotFoundException;
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
  @Subscribe(async = false)
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

        try {
          repositoryManager.modify(dbr);
        } catch (NotFoundException e) {
          logger.error("could not modify repository", e);
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
