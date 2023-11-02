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

import com.google.inject.assistedinject.Assisted;
import sonia.scm.repository.Modification;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.spi.javahg.HgLogChangesetCommand;
import sonia.scm.repository.spi.javahg.StateCommand;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;

public class HgModificationsCommand extends AbstractCommand implements ModificationsCommand {

  @Inject
  HgModificationsCommand(@Assisted HgCommandContext context) {
    super(context);
  }

  @Override
  public Modifications getModifications(String revision) {
    org.javahg.Repository repository = open();
    HgLogChangesetCommand hgLogChangesetCommand = HgLogChangesetCommand.on(repository, getContext().getConfig());
    Collection<Modification> modifications = hgLogChangesetCommand.rev(revision).extractModifications();
    return new Modifications(revision, modifications);
  }

  @Override
  public Modifications getModifications(String baseRevision, String revision) throws IOException {
    org.javahg.Repository repository = open();
    StateCommand stateCommand = new StateCommand(repository);
    return new Modifications(baseRevision, revision, stateCommand.call(baseRevision, revision));
  }

  public interface Factory {
    HgModificationsCommand create(HgCommandContext context);
  }

}
