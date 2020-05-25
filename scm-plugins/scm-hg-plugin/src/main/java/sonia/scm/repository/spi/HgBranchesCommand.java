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

package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.Changeset;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import sonia.scm.repository.Branch;

import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class HgBranchesCommand extends AbstractCommand
  implements BranchesCommand
{

  private static final String DEFAULT_BRANCH_NAME = "default";

  /**
   * Constructs ...
   *
   *  @param context
   *
   */
  public HgBranchesCommand(HgCommandContext context)
  {
    super(context);
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  public List<Branch> getBranches() {
    List<com.aragost.javahg.commands.Branch> hgBranches =
      com.aragost.javahg.commands.BranchesCommand.on(open()).execute();

    List<Branch> branches = Lists.transform(hgBranches,
                              new Function<com.aragost.javahg.commands.Branch,
                                Branch>()
    {

      @Override
      public Branch apply(com.aragost.javahg.commands.Branch hgBranch)
      {
        String node = null;
        Changeset changeset = hgBranch.getBranchTip();

        if (changeset != null)
        {
          node = changeset.getNode();
        }

        if (DEFAULT_BRANCH_NAME.equals(hgBranch.getName())) {
          return Branch.defaultBranch(hgBranch.getName(), node);
        } else {
          return Branch.normalBranch(hgBranch.getName(), node);
        }
      }
    });

    return branches;
  }
}
