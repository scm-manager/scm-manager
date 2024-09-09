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

import sonia.scm.repository.HgConfig;


public class HgIncomingChangesetCommand
  extends HgIncomingOutgoingChangesetCommand
{

 
  private HgIncomingChangesetCommand(Repository repository, HgConfig config)
  {
    super(repository, config);
  }



  public static HgIncomingChangesetCommand on(Repository repository, HgConfig config) {
    return new HgIncomingChangesetCommand(repository, config);
  }


  
  @Override
  public String getCommandName()
  {
    return "incoming";
  }
}
