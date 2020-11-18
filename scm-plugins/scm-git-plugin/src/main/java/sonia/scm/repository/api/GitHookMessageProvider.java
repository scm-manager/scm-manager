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

package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.transport.ReceivePack;

import sonia.scm.web.GitHooks;

/**
 *
 * @author Sebastian Sdorra
 */
public final class GitHookMessageProvider implements HookMessageProvider
{

  /**
   * Constructs ...
   *
   *
   * @param receivePack
   */
  public GitHookMessageProvider(ReceivePack receivePack)
  {
    this.receivePack = receivePack;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param message
   */
  @Override
  public void sendError(String message)
  {
    GitHooks.sendPrefixedError(receivePack, message);
  }

  /**
   * Method description
   *
   *
   * @param message
   */
  @Override
  public void sendMessage(String message)
  {
    if (!receivePack.isQuiet()) {
      GitHooks.sendPrefixedMessage(receivePack, message);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ReceivePack receivePack;
}
