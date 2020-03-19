/**
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

package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.spi.javahg.HgLogChangesetCommand;
import sonia.scm.web.HgUtil;

import java.io.File;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class HgHookChangesetProvider implements HookChangesetProvider
{

  /**
   * the logger for HgHookChangesetProvider
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HgHookChangesetProvider.class);

  //~--- constructors ---------------------------------------------------------

  public HgHookChangesetProvider(HgRepositoryHandler handler,
    File repositoryDirectory, HgHookManager hookManager, String startRev,
    RepositoryHookType type)
  {
    this.handler = handler;
    this.repositoryDirectory = repositoryDirectory;
    this.hookManager = hookManager;
    this.startRev = startRev;
    this.type = type;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @Override
  public synchronized HookChangesetResponse handleRequest(HookChangesetRequest request)
  {
    if (response == null)
    {
      Repository repository = null;

      try
      {
        repository = open();

        HgLogChangesetCommand cmd = HgLogChangesetCommand.on(repository,
                                      handler.getConfig());

        response = new HookChangesetResponse(
          cmd.rev(startRev.concat(":").concat(HgUtil.REVISION_TIP)).execute());
      }
      catch (Exception ex)
      {
        logger.error("could not retrieve changesets", ex);
      }
      finally
      {
        if (repository != null)
        {
          repository.close();
        }
      }
    }

    return response;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private Repository open()
  {
    // use HG_PENDING only for pre receive hooks
    boolean pending = type == RepositoryHookType.PRE_RECEIVE;

    // TODO get repository encoding
    return HgUtil.open(handler, hookManager, repositoryDirectory, null,
      pending);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private HgHookManager hookManager;

  /** Field description */
  private File repositoryDirectory;

  /** Field description */
  private HookChangesetResponse response;

  /** Field description */
  private String startRev;

  /** Field description */
  private RepositoryHookType type;
}
