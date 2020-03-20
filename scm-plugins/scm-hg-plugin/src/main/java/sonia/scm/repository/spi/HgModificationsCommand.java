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

import sonia.scm.repository.Modifications;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.javahg.HgLogChangesetCommand;

public class HgModificationsCommand extends AbstractCommand implements ModificationsCommand {

  HgModificationsCommand(HgCommandContext context, Repository repository) {
    super(context, repository);
  }


  @Override
  public Modifications getModifications(String revision) {
    com.aragost.javahg.Repository repository = open();
    HgLogChangesetCommand hgLogChangesetCommand = HgLogChangesetCommand.on(repository, getContext().getConfig());
    Modifications modifications = hgLogChangesetCommand.rev(revision).extractModifications();
    modifications.setRevision(revision);
    return modifications;
  }

  @Override
  public Modifications getModifications(ModificationsCommandRequest request) {
    return getModifications(request.getRevision());
  }


}
