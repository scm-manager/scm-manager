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

package sonia.scm.repository.spi.javahg;


import org.javahg.Repository;
import org.javahg.internals.HgInputStream;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.HgConfig;
import sonia.scm.util.IOUtil;

import java.util.List;


public abstract class HgIncomingOutgoingChangesetCommand
  extends AbstractChangesetCommand
{

 
  public HgIncomingOutgoingChangesetCommand(Repository repository, HgConfig config) {
    super(repository, config);
  }



  public List<Changeset> execute(String remoteRepository)
  {
    cmdAppend("--style", CHANGESET_EAGER_STYLE_PATH);

    List<Changeset> changesets = null;
    HgInputStream stream = null;

    try
    {
      stream = launchStream(remoteRepository);
      changesets = readListFromStream(stream);
    }
    finally
    {
      IOUtil.close(stream);
    }

    return changesets;
  }
}
