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

package sonia.scm.repository;


import sonia.scm.SCMContextProvider;
import sonia.scm.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;


public class TempSCMContextProvider implements SCMContextProvider
{

  
  @Override
  public File getBaseDirectory()
  {
    return baseDirectory;
  }

  
  @Override
  public Stage getStage()
  {
    return Stage.DEVELOPMENT;
  }

  
  @Override
  public Throwable getStartupError()
  {
    return null;
  }

  
  @Override
  public String getVersion()
  {
    return "900.0.1-SNAPSHOT";
  }



  public void setBaseDirectory(File baseDirectory)
  {
    this.baseDirectory = baseDirectory;
  }

  @Override
  public Path resolve(Path path) {
    return baseDirectory.toPath().resolve(path);
  }

  //~--- fields ---------------------------------------------------------------

  private File baseDirectory;
}
