/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi.javahg;

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.Repository;
import com.aragost.javahg.internals.AbstractCommand;
import com.aragost.javahg.internals.HgInputStream;
import com.aragost.javahg.internals.Utils;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 */
public final class HgDiffInternalCommand extends AbstractCommand
{

  /** Field description */
  private static final String NAME = "diff";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repository
   */
  private HgDiffInternalCommand(Repository repository)
  {
    super(repository, NAME);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  public static HgDiffInternalCommand on(Repository repository)
  {
    return new HgDiffInternalCommand(repository);
  }

  /**
   * Method description
   *
   *
   * @param rev
   *
   * @return
   */
  public HgDiffInternalCommand change(String rev)
  {
    cmdAppend("--change", rev);

    return this;
  }

  /**
   * Method description
   *
   *
   * @param files
   *
   * @return
   */
  public String execute(String... files)
  {
    return launchString(files);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public HgDiffInternalCommand git()
  {
    cmdAppend("--git");

    return this;
  }

  /**
   * Method description
   *
   *
   * @param files
   *
   * @return
   */
  public HgInputStream stream(File... files)
  {
    return launchStream(Utils.fileArray2StringArray(files));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getCommandName()
  {
    return NAME;
  }
}
