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
import org.javahg.internals.AbstractCommand;
import org.javahg.internals.HgInputStream;
import org.javahg.internals.Utils;

import java.io.File;


public final class HgDiffInternalCommand extends AbstractCommand
{

  private static final String NAME = "diff";


 
  private HgDiffInternalCommand(Repository repository)
  {
    super(repository, NAME);
  }



  public static HgDiffInternalCommand on(Repository repository)
  {
    return new HgDiffInternalCommand(repository);
  }


  public HgDiffInternalCommand change(String rev)
  {
    cmdAppend("--change", rev);

    return this;
  }


  public String execute(String... files)
  {
    return launchString(files);
  }

  
  public HgDiffInternalCommand git()
  {
    cmdAppend("--git");

    return this;
  }


  public HgInputStream stream(File... files)
  {
    return launchStream(Utils.fileArray2StringArray(files));
  }


  
  @Override
  public String getCommandName()
  {
    return NAME;
  }
}
