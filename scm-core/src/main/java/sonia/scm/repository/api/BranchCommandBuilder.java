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

package sonia.scm.repository.api;

import sonia.scm.repository.Branch;
import sonia.scm.repository.spi.BranchCommand;

import java.io.IOException;

/**
 * @since 2.0
 */
public final class BranchCommandBuilder {

  public BranchCommandBuilder(BranchCommand command) {
    this.command = command;
  }

  /**
   * Specifies the source branch, which the new branch should be based on.
   *
   * @param parentBranch The base branch for the new branch.
   * @return This builder.
   */
  public BranchCommandBuilder from(String parentBranch) {
    request.setParentBranch(parentBranch);
    return this;
  }

  /**
   * Execute the command and create a new branch with the given name.
   * @param name The name of the new branch.
   * @return The created branch.
   * @throws IOException
   */
  public Branch branch(String name) {
    request.setNewBranch(name);
    return command.branch(request);
  }

  public void delete(String branchName) {
    command.deleteOrClose(branchName);
  }

  private BranchCommand command;
  private BranchRequest request = new BranchRequest();
}
