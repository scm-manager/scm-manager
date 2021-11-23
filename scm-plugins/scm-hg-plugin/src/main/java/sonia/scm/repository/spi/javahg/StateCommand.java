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
import sonia.scm.repository.Modification;

import java.io.IOException;
import java.util.Collection;

public class StateCommand extends org.javahg.internals.AbstractCommand {
  public StateCommand(Repository repository) {
    super(repository);
  }

  @Override
  public String getCommandName() {
    return "status";
  }

  public Collection<Modification> call(String from, String to) throws IOException {
    cmdAppend("--rev", from + ":" + to);
    HgInputStream in = launchStream();
    HgModificationParser hgModificationParser = new HgModificationParser();
    String line = in.textUpTo('\n');
    while (line != null && line.length() > 0) {
      hgModificationParser.addLine(line);
      line = in.textUpTo('\n');
    }
    return hgModificationParser.getModifications();
  }
}
