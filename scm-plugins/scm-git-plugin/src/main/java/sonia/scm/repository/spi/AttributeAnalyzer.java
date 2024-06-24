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
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.attributes.Attributes;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.LfsFactory;
import sonia.scm.NotFoundException;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Modified;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
class AttributeAnalyzer {

  private static final String MERGE_TOOL_ATTRIBUTE_KEY = "merge";
  private final GitContext context;
  private final GitModificationsCommand modificationsCommand;

  @Inject
  AttributeAnalyzer(@Assisted GitContext context, GitModificationsCommand modificationsCommand) {
    this.context = context;
    this.modificationsCommand = modificationsCommand;
  }

  Optional<Attributes> getAttributes(RevCommit commit, String path) throws NotFoundException {
    try (Repository repository = context.open()) {
      Attributes attributesForPath = LfsFactory.getAttributesForPath(repository, path, commit);
      if (attributesForPath.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(attributesForPath);
    } catch (IOException e) {
      log.debug("Failed to get attributes", e);
      return Optional.empty();
    }
  }

  boolean hasExternalMergeToolConflicts(String source, String target) {
    try (Repository repo = context.open()) {
      String commonAncestorRevision = GitUtil.computeCommonAncestor(repo, GitUtil.getRevisionId(repo, source), GitUtil.getRevisionId(repo, target)).name();
      return findExternalMergeToolConflicts(source, target, commonAncestorRevision);
    } catch (IOException | NotFoundException e) {
      log.debug("Failed to read/parse '.gitattributes' files", e);
      return false;
    }
  }

  boolean findExternalMergeToolConflicts(String source, String target, String commonAncestor) throws IOException {
    List<String> changesInBoth = getPossiblyAffectedPaths(source, target, commonAncestor);
    RevCommit targetCommit = getTargetCommit(target);

    for (String path : changesInBoth) {
      Optional<Attributes> attributes = getAttributes(targetCommit, path);
      if (attributes.isPresent() && attributes.get().get(MERGE_TOOL_ATTRIBUTE_KEY) != null) {
        return true;
      }
    }
    return false;
  }

  private List<String> getPossiblyAffectedPaths(String source, String target, String commonAncestor) {
    Collection<String> fromSourceToAncestor = findModifiedPaths(source, commonAncestor);
    Collection<String> fromTargetToAncestor = findModifiedPaths(target, commonAncestor);
    return fromSourceToAncestor.stream().filter(fromTargetToAncestor::contains).toList();
  }

  private Collection<String> findModifiedPaths(String baseRevision, String targetRevision) {
    return modificationsCommand.getModifications(baseRevision, targetRevision).getModified()
      .stream().map(Modified::getPath).collect(Collectors.toSet());
  }

  RevCommit getTargetCommit(String target) throws IOException {
    try (Repository repository = context.open()) {
      RevWalk rw = new org.eclipse.jgit.revwalk.RevWalk(repository);
      return GitUtil.getCommit(repository, rw, repository.findRef(target));
    } catch (IOException e) {
      log.debug("Failed to get target commit", e);
      throw e;
    }
  }

  public interface Factory {
    AttributeAnalyzer create(GitContext context);
  }
}
