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

import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;

/**
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
 */
public class AbstractHgPushOrPullCommand extends AbstractCommand
{

  /**
   * Constructs ...
   *
   *  @param handler
   * @param context
   */
  protected AbstractHgPushOrPullCommand(HgRepositoryHandler handler, HgCommandContext context)
  {
    super(context);
    this.handler = handler;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  protected String getRemoteUrl(RemoteCommandRequest request)
  {
    String url;
    Repository repo = request.getRemoteRepository();

    if (repo != null)
    {
      url =
        handler.getDirectory(request.getRemoteRepository().getId()).getAbsolutePath();
    }
    else if (request.getRemoteUrl() != null)
    {
      url = request.getRemoteUrl().toExternalForm();
    }
    else
    {
      throw new IllegalArgumentException("url or repository is required");
    }

    return url;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected final HgRepositoryHandler handler;
}
