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
