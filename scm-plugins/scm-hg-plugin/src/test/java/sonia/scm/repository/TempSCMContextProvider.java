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
