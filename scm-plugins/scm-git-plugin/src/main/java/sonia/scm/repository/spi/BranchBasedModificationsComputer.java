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
