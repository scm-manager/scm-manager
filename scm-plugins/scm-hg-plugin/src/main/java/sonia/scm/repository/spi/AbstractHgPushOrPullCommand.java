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

package sonia.scm.repository.spi;


import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;

public class AbstractHgPushOrPullCommand extends AbstractCommand
{
  protected final HgRepositoryHandler handler;

  protected AbstractHgPushOrPullCommand(HgRepositoryHandler handler, HgCommandContext context)
  {
    super(context);
    this.handler = handler;
  }



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

}
