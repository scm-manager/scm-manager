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
    command.delete(branchName);
  }

  private BranchCommand command;
  private BranchRequest request = new BranchRequest();
}
