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

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.spi.javahg.HgDiffInternalCommand;
import sonia.scm.web.HgUtil;

import java.io.InputStream;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class HgDiffCommand extends AbstractCommand implements DiffCommand
{

  /**
   * Constructs ...
   *
   *  @param context
   *
   */
  HgDiffCommand(HgCommandContext context)
  {
    super(context);
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  public DiffCommandBuilder.OutputStreamConsumer getDiffResult(DiffCommandRequest request)
  {
    return output -> {
      com.aragost.javahg.Repository hgRepo = open();

      HgDiffInternalCommand cmd = HgDiffInternalCommand.on(hgRepo);
      DiffFormat format = request.getFormat();

      if (format == DiffFormat.GIT)
      {
        cmd.git();
      }

      cmd.change(HgUtil.getRevision(request.getRevision()));

      InputStream inputStream = null;

      try {

        if (!Strings.isNullOrEmpty(request.getPath())) {
          inputStream = cmd.stream(hgRepo.file(request.getPath()));
        } else {
          inputStream = cmd.stream();
        }

        ByteStreams.copy(inputStream, output);

      } finally {
        Closeables.close(inputStream, true);
      }
    };
  }
}
