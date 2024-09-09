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

package sonia.scm.repository.api;


import org.eclipse.jgit.transport.ReceivePack;

import sonia.scm.web.GitHooks;


public final class GitHookMessageProvider implements HookMessageProvider
{
  private ReceivePack receivePack;
 
  public GitHookMessageProvider(ReceivePack receivePack)
  {
    this.receivePack = receivePack;
  }



  @Override
  public void sendError(String message)
  {
    GitHooks.sendPrefixedError(receivePack, message);
  }


  @Override
  public void sendMessage(String message)
  {
    if (!receivePack.isQuiet()) {
      GitHooks.sendPrefixedMessage(receivePack, message);
    }
  }

}
