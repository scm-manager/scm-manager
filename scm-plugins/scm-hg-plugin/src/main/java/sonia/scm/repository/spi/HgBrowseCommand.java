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

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.javahg.Changeset;
import org.javahg.commands.LogCommand;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.spi.javahg.HgFileviewCommand;

import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

//~--- JDK imports ------------------------------------------------------------

/**
 * Utilizes the mercurial fileview extension in order to support mercurial repository browsing.
 *
 * @author Sebastian Sdorra
 */
public class HgBrowseCommand extends AbstractCommand implements BrowseCommand
{

  /**
   * Constructs ...
   *
   *  @param context
   *
   */

  @Inject
  public HgBrowseCommand(@Assisted HgCommandContext context)
  {
    super(context);
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  public BrowserResult getBrowserResult(BrowseCommandRequest request) throws IOException {
    HgFileviewCommand cmd = HgFileviewCommand.on(open());

    String revision = MoreObjects.firstNonNull(request.getRevision(), "tip");
    Changeset c = LogCommand.on(getContext().open()).rev(revision).limit(1).single();

    if (c != null) {
      cmd.rev(c.getNode());
    }

    if (!Strings.isNullOrEmpty(request.getPath()))
    {
      cmd.path(request.getPath());
    }

    if (request.isDisableLastCommit())
    {
      cmd.disableLastCommit();
    }

    if (request.isRecursive())
    {
      cmd.recursive();
    }

    if (request.isDisableSubRepositoryDetection())
    {
      cmd.disableSubRepositoryDetection();
    }

    cmd.setLimit(request.getLimit());
    cmd.setOffset(request.getOffset());

    FileObject file = cmd.execute()
      .orElseThrow(() -> notFound(entity("File", request.getPath()).in("Revision", revision).in(getRepository())));
    return new BrowserResult(c == null? "tip": c.getNode(), revision, file);
  }

  public interface Factory {
    HgBrowseCommand create(HgCommandContext context);
  }

}
