/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
