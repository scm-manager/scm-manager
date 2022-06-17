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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lfs.Lfs;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.Protocol;
import org.eclipse.jgit.lfs.SmudgeFilter;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.LfsPointerFilter;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.store.BlobStore;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import javax.inject.Inject;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

class LfsLoader {

  private static final Logger LOG = LoggerFactory.getLogger(LfsLoader.class);

  private final LfsBlobStoreFactory lfsBlobStoreFactory;
  private final MirrorHttpConnectionProvider mirrorHttpConnectionProvider;

  @Inject
  LfsLoader(LfsBlobStoreFactory lfsBlobStoreFactory, MirrorHttpConnectionProvider mirrorHttpConnectionProvider) {
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
    this.mirrorHttpConnectionProvider = mirrorHttpConnectionProvider;
  }

  void inspectTree(ObjectId newObjectId, MirrorCommandRequest mirrorCommandRequest, Git git, List<String> mirrorLog, MirrorCommandResult.LfsUpdateResult lfsUpdateResult, sonia.scm.repository.Repository repository) {

    String mirrorUrl = mirrorCommandRequest.getSourceUrl();

    try {
      Repository repo = git.getRepository();

      repo.getConfig().setString(ConfigConstants.CONFIG_SECTION_LFS, null, ConfigConstants.CONFIG_KEY_URL, mirrorUrl + ".git" + Protocol.INFO_LFS_ENDPOINT);

      TreeWalk treeWalk = new TreeWalk(repo);
      treeWalk.setFilter(new LfsPointerFilter());

      RevWalk revWalk = new RevWalk(repo);
      revWalk.markStart(revWalk.parseCommit(newObjectId));
      BlobStore lfsBlobStore = lfsBlobStoreFactory.getLfsBlobStore(repository);
      HttpConnectionFactory httpConnectionFactory = mirrorHttpConnectionProvider.createHttpConnectionFactory(mirrorCommandRequest, mirrorLog);

      for (RevCommit commit : revWalk) {
        treeWalk.reset();
        treeWalk.addTree(commit.getTree());
        while (treeWalk.next()) {
          treeWalk.getNameString();
          try (InputStream is = repo.open(treeWalk.getObjectId(0), Constants.OBJ_BLOB).openStream()) {
            LfsPointer lfsPointer = LfsPointer.parseLfsPointer(is);
            AnyLongObjectId oid = lfsPointer.getOid();

            if (lfsBlobStore.get(oid.name()) == null) {
              lfsUpdateResult.increaseOverallCount();
              LOG.trace("trying to load lfs file '{}' for repository {}", oid.name(), repository);
              mirrorLog.add(String.format("Loading lfs file with id '%s'", oid.name()));
              Lfs lfs = new Lfs(repo);
              lfs.getMediaFile(oid);

              Collection<Path> paths = SmudgeFilter.downloadLfsResource(
                lfs,
                repo,
                httpConnectionFactory,
                lfsPointer
              );
              Path tempFilePath = paths.iterator().next();
              LOG.trace("temporary lfs file: {}", tempFilePath);
              Files.copy(
                tempFilePath,
                lfsBlobStore
                  .create(oid.name())
                  .getOutputStream()
              );
              Files.delete(tempFilePath);
            }
          } catch (Exception e) {
            LOG.warn("failed to load lfs file", e);
            mirrorLog.add("Failed to load lfs file:");
            mirrorLog.add(e.getMessage());
            lfsUpdateResult.increaseFailureCount();
          }
        }
      }
    } catch (Exception e) {
      LOG.warn("failed to load lfs files", e);
      mirrorLog.add("Failed to load lfs files:");
      mirrorLog.add(e.getMessage());
      lfsUpdateResult.increaseFailureCount();
    }
  }
}
