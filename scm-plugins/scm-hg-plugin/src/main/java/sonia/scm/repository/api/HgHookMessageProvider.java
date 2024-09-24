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


import com.google.common.collect.Lists;

import sonia.scm.repository.api.HgHookMessage.Severity;

import java.util.List;


public class HgHookMessageProvider implements HookMessageProvider
{
  private List<HgHookMessage> messages;

  @Override
  public void sendError(String message)
  {
    getMessages().add(new HgHookMessage(Severity.ERROR, message));
  }


  @Override
  public void sendMessage(String message)
  {
    getMessages().add(new HgHookMessage(Severity.NOTE, message));
  }


  
  public List<HgHookMessage> getMessages()
  {
    if (messages == null)
    {
      messages = Lists.newArrayList();
    }

    return messages;
  }

}
