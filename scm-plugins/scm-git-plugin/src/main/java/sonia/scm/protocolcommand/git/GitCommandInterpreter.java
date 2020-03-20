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
    
package sonia.scm.protocolcommand.git;

import sonia.scm.protocolcommand.CommandInterpreter;
import sonia.scm.protocolcommand.RepositoryContextResolver;
import sonia.scm.protocolcommand.ScmCommandProtocol;

class GitCommandInterpreter implements CommandInterpreter {
  private final GitRepositoryContextResolver gitRepositoryContextResolver;
  private final GitCommandProtocol gitCommandProtocol;
  private final String[] args;

  GitCommandInterpreter(GitRepositoryContextResolver gitRepositoryContextResolver, GitCommandProtocol gitCommandProtocol, String[] args) {
    this.gitRepositoryContextResolver = gitRepositoryContextResolver;
    this.gitCommandProtocol = gitCommandProtocol;
    this.args = args;
  }

  @Override
  public String[] getParsedArgs() {
    return args;
  }

  @Override
  public ScmCommandProtocol getProtocolHandler() {
    return gitCommandProtocol;
  }

  @Override
  public RepositoryContextResolver getRepositoryContextResolver() {
    return gitRepositoryContextResolver;
  }
}
