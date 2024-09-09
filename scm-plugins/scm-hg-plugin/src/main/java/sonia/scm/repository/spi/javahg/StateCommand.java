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
