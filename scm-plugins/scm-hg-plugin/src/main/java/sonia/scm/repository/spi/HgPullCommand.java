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
    
package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.Changeset;
import com.aragost.javahg.commands.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.PullResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class HgPullCommand extends AbstractHgPushOrPullCommand
  implements PullCommand
{

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(HgPullCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param context
   * @param repository
   */
  public HgPullCommand(HgRepositoryHandler handler, HgCommandContext context,
    Repository repository)
  {
    super(handler, context, repository);
  }

  //~--- methods --------------------------------------------------------------

  @Override
  @SuppressWarnings("unchecked")
  public PullResponse pull(PullCommandRequest request)
    throws IOException
  {
    String url = getRemoteUrl(request);

    logger.debug("pull changes from {} to {}", url, getRepository().getId());

    List<Changeset> result = Collections.EMPTY_LIST;

    try
    {
      result = com.aragost.javahg.commands.PullCommand.on(open()).execute(url);
    }
    catch (ExecutionException ex)
    {
      throw new InternalRepositoryException(getRepository(), "could not execute push command", ex);
    }

    return new PullResponse(result.size());
  }
}
