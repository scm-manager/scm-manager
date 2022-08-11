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

import sonia.scm.repository.Changeset;
import sonia.scm.repository.spi.javahg.HgLogChangesetCommand;

import static sonia.scm.repository.spi.javahg.HgLogChangesetCommand.on;

public class HgChangesetsCommand extends AbstractCommand implements ChangesetsCommand {

  public HgChangesetsCommand(HgCommandContext context) {
    super(context);
  }

  @Override
  public Iterable<Changeset> getChangesets(ChangesetsCommandRequest request) {
    org.javahg.Repository repository = open();
    HgLogChangesetCommand cmd = on(repository, context.getConfig());
    // Get all changesets between the first changeset and the repository tip, both inclusive.
    cmd.rev("tip:0");
    return cmd.execute();
  }
}
