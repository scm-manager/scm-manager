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

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import sonia.scm.repository.GitHookChangesetCollector;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitHookChangesetProvider implements HookChangesetProvider
{

  /**
   * Constructs ...
   *
   *
   * @param receivePack
   * @param receiveCommands
   */
  public GitHookChangesetProvider(ReceivePack receivePack,
    List<ReceiveCommand> receiveCommands)
  {
    this.receivePack = receivePack;
    this.receiveCommands = receiveCommands;
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
  public synchronized HookChangesetResponse handleRequest(
    HookChangesetRequest request)
  {
    if (response == null)
    {
      GitHookChangesetCollector collector =
        new GitHookChangesetCollector(receivePack, receiveCommands);

      response = new HookChangesetResponse(collector.collectChangesets());
    }

    return response;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private List<ReceiveCommand> receiveCommands;

  /** Field description */
  private ReceivePack receivePack;

  /** Field description */
  private HookChangesetResponse response;
}
