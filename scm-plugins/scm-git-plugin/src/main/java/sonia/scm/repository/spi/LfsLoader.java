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

import jakarta.inject.Inject;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lfs.Lfs;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.Protocol;
import org.eclipse.jgit.lfs.SmudgeFilter;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.LfsPointerFilter;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.api.MirrorCommandResult.LfsUpdateResult;
import sonia.scm.store.BlobStore;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

class LfsLoader {

  private static final Logger LOG = LoggerFactory.getLogger(LfsLoader.class);

  private final LfsBlobStoreFactory lfsBlobStoreFactory;

  @Inject
  LfsLoader(LfsBlobStoreFactory lfsBlobStoreFactory) {
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
  }

  void inspectTree(ObjectId newObjectId,
                   Repository gitRepository,
                   LfsLoaderLogger mirrorLog,
                   LfsUpdateResult lfsUpdateResult,
                   sonia.scm.repository.Repository repository,
                   HttpConnectionFactory httpConnectionFactory,
                   String url) {
    EntryHandler entryHandler = new EntryHandler(repository, gitRepository, mirrorLog, lfsUpdateResult, httpConnectionFactory);
    inspectTree(newObjectId, entryHandler, gitRepository, mirrorLog, lfsUpdateResult, url);
  }

  private void inspectTree(ObjectId newObjectId,
                           EntryHandler entryHandler,
                           Repository gitRepository,
                           LfsLoaderLogger mirrorLog,
                           LfsUpdateResult lfsUpdateResult,
                           String sourceUrl) {
    try {
      gitRepository
        .getConfig()
        .setString(ConfigConstants.CONFIG_SECTION_LFS, null, ConfigConstants.CONFIG_KEY_URL, computeLfsUrl(sourceUrl));

      TreeWalk treeWalk = new TreeWalk(gitRepository);
      treeWalk.setFilter(new ScmLfsPointerFilter());
      treeWalk.setRecursive(true);

      RevWalk revWalk = new RevWalk(gitRepository);
      revWalk.markStart(revWalk.parseCommit(newObjectId));

      for (RevCommit commit : revWalk) {
        treeWalk.reset();
        treeWalk.addTree(commit.getTree());
        while (treeWalk.next()) {
          entryHandler.handleTreeEntry(treeWalk);
        }
      }
    } catch (Exception e) {
      LOG.warn("failed to load lfs files", e);
      mirrorLog.failed(e);
      lfsUpdateResult.increaseFailureCount();
    }
  }

  private String computeLfsUrl(String sourceUrl) {
    if (sourceUrl.endsWith(".git")) {
      return sourceUrl + Protocol.INFO_LFS_ENDPOINT;
    } else {
      return sourceUrl + ".git" + Protocol.INFO_LFS_ENDPOINT;
    }
  }

  private class EntryHandler {

    private final BlobStore lfsBlobStore;
    private final Repository gitRepository;
    private final LfsLoaderLogger mirrorLog;
    private final LfsUpdateResult lfsUpdateResult;
    private final sonia.scm.repository.Repository repository;
    private final HttpConnectionFactory httpConnectionFactory;

    private EntryHandler(sonia.scm.repository.Repository repository,
                         Repository gitRepository,
                         LfsLoaderLogger mirrorLog,
                         LfsUpdateResult lfsUpdateResult,
                         HttpConnectionFactory httpConnectionFactory) {
      this.lfsBlobStore = lfsBlobStoreFactory.getLfsBlobStore(repository);
      this.repository = repository;
      this.gitRepository = gitRepository;
      this.mirrorLog = mirrorLog;
      this.lfsUpdateResult = lfsUpdateResult;
      this.httpConnectionFactory = httpConnectionFactory;
    }

    private void handleTreeEntry(TreeWalk treeWalk) {
      try (InputStream is = gitRepository.open(treeWalk.getObjectId(0), Constants.OBJ_BLOB).openStream()) {
        LfsPointer lfsPointer = LfsPointer.parseLfsPointer(is);
        AnyLongObjectId oid = lfsPointer.getOid();

        if (lfsBlobStore.get(oid.name()) == null) {
          Path tempFilePath = loadLfsFile(lfsPointer);
          storeLfsBlob(oid, tempFilePath);
          Files.delete(tempFilePath);
        }
      } catch (Exception e) {
        LOG.warn("failed to load lfs file", e);
        mirrorLog.failed(e);
        lfsUpdateResult.increaseFailureCount();
      }
    }

    private Path loadLfsFile(LfsPointer lfsPointer) throws IOException {
      lfsUpdateResult.increaseOverallCount();
      LOG.trace("trying to load lfs file '{}' for repository {}", lfsPointer.getOid(), repository);
      mirrorLog.loading(lfsPointer.getOid().name());
      Lfs lfs = new Lfs(gitRepository);
      lfs.getMediaFile(lfsPointer.getOid());

      Collection<Path> paths = SmudgeFilter.downloadLfsResource(
        lfs,
        gitRepository,
        httpConnectionFactory,
        lfsPointer
      );
      return paths.iterator().next();
    }

    private void storeLfsBlob(AnyLongObjectId oid, Path tempFilePath) throws IOException {
      LOG.trace("temporary lfs file: {}", tempFilePath);
      Files.copy(
        tempFilePath,
        lfsBlobStore
          .create(oid.name())
          .getOutputStream()
      );
    }
  }

  interface LfsLoaderLogger {

    void failed(Exception e);

    void loading(String name);
  }

  /**
   * Fixes a bug in {@link org.eclipse.jgit.lfs.lib.LfsPointerFilter} for repositories containing submodules.
   * These result in a {@link MissingObjectException} when the original class is used, because the filter tries
   * to load the sha hash for the submodule as a simple file. To prevent this, this extension overrides
   * {@link #include(TreeWalk)} and checks first, whether the walk points to a regular file before proceeding
   * with the original implemantation.
   *
   * In later implementations this fix should be implemented in JGit directly. This subclass can then be removed.
   */
  private static class ScmLfsPointerFilter extends LfsPointerFilter {

    @Override
    public boolean include(TreeWalk walk) throws IOException {
      if (walk.getFileMode().equals(FileMode.GITLINK)) {
        return false;
      }
      return super.include(walk);
    }
  }
}
