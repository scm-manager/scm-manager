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
import org.javahg.internals.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.Modification;

import java.io.IOException;
import java.util.Collection;
import java.util.List;


public class HgLogChangesetCommand extends AbstractChangesetCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgLogChangesetCommand.class);

  private HgLogChangesetCommand(Repository repository, HgConfig config) {
    super(repository, config);
  }


  public static HgLogChangesetCommand on(Repository repository, HgConfig config) {
    return new HgLogChangesetCommand(repository, config);
  }


  public HgLogChangesetCommand branch(String branch) {
    cmdAppend("-b", branch);

    return this;
  }


  public List<Changeset> execute(String... files) {
    return readListFromStream(getHgInputStream(files, CHANGESET_EAGER_STYLE_PATH));
  }

  public Collection<Modification> extractModifications(String... files) {
    HgInputStream hgInputStream = getHgInputStream(files, CHANGESET_EAGER_STYLE_PATH);
    try {
      return readModificationsFromStream(hgInputStream);
    } finally {
      try {
        hgInputStream.close();
      } catch (IOException e) {
        LOG.error("Could not close HgInputStream", e);
      }
    }
  }

  HgInputStream getHgInputStream(String[] files, String changesetStylePath) {
    cmdAppend("--style", changesetStylePath);
    return launchStream(files);
  }

  public HgLogChangesetCommand limit(int limit) {
    cmdAppend("-l", limit);

    return this;
  }


  public List<Integer> loadRevisions(String... files) {
    return loadRevisionsFromStream(getHgInputStream(files, CHANGESET_LAZY_STYLE_PATH));
  }

  public HgLogChangesetCommand rev(String... rev) {
    cmdAppend("-r", rev);

    return this;
  }

  public Changeset single(String... files) {
    return Utils.single(execute(files));
  }

  public int singleRevision(String... files) {
    Integer rev = Utils.single(loadRevisions(files));

    if (rev == null) {
      rev = -1;
    }

    return rev;
  }

  @Override
  public String getCommandName() {
    return "log";
  }
}
