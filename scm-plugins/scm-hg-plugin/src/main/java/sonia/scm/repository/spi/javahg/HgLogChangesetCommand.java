/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
