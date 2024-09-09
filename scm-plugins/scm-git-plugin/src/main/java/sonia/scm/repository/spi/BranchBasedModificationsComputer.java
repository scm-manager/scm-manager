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

package sonia.scm.repository.spi;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import sonia.scm.repository.Modification;
import sonia.scm.repository.Modifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class BranchBasedModificationsComputer {

  private final Repository repository;

  public BranchBasedModificationsComputer(Repository repository) {
    this.repository = repository;
  }

  public Modifications createModifications(ObjectId revision, Function<String, Modification> modificationFactory) throws IOException {
    Collection<Modification> modifications = new ArrayList<>();
    try (RevWalk revWalk = new RevWalk(repository); TreeWalk treeWalk = new TreeWalk(repository)) {
      RevTree tree = revWalk.parseTree(revision);
      treeWalk.addTree(tree);
      while (treeWalk.next()) {
        if (treeWalk.isSubtree()) {
          treeWalk.enterSubtree();
        } else {
          modifications.add(modificationFactory.apply(treeWalk.getPathString()));
        }
      }
    }
    return new Modifications(revision.name(), modifications);
  }
}
