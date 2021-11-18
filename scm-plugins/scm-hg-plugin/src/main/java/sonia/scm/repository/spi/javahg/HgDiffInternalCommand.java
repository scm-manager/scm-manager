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
    
package sonia.scm.repository.spi.javahg;

//~--- non-JDK imports --------------------------------------------------------

import org.javahg.Repository;
import org.javahg.internals.AbstractCommand;
import org.javahg.internals.HgInputStream;
import org.javahg.internals.Utils;

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
