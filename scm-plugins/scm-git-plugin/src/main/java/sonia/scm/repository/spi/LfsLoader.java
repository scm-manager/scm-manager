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
import sonia.scm.repository.api.MirrorCommandResult.LfsUpdateResult;
import sonia.scm.store.BlobStore;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import javax.inject.Inject;
import java.io.IOException;
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

  void inspectTree(ObjectId newObjectId,
                   MirrorCommandRequest mirrorCommandRequest,
                   Repository gitRepository,
                   List<String> mirrorLog,
                   LfsUpdateResult lfsUpdateResult,
                   sonia.scm.repository.Repository repository) {
    String mirrorUrl = mirrorCommandRequest.getSourceUrl();
    EntryHandler entryHandler = new EntryHandler(repository, gitRepository, mirrorCommandRequest, mirrorLog, lfsUpdateResult);

    try {
      gitRepository
        .getConfig()
        .setString(ConfigConstants.CONFIG_SECTION_LFS, null, ConfigConstants.CONFIG_KEY_URL, computeLfsUrl(mirrorUrl));

      TreeWalk treeWalk = new TreeWalk(gitRepository);
      treeWalk.setFilter(new LfsPointerFilter());

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
      mirrorLog.add("Failed to load lfs files:");
      mirrorLog.add(e.getMessage());
      lfsUpdateResult.increaseFailureCount();
    }
  }

  private String computeLfsUrl(String mirrorUrl) {
    if (mirrorUrl.endsWith(".git")) {
      return mirrorUrl + Protocol.INFO_LFS_ENDPOINT;
    } else {
      return mirrorUrl + ".git" + Protocol.INFO_LFS_ENDPOINT;
    }
  }

  private class EntryHandler {

    private final BlobStore lfsBlobStore;
    private final Repository gitRepository;
    private final List<String> mirrorLog;
    private final LfsUpdateResult lfsUpdateResult;
    private final sonia.scm.repository.Repository repository;
    private final HttpConnectionFactory httpConnectionFactory;

    private EntryHandler(sonia.scm.repository.Repository repository,
                         Repository gitRepository,
                         MirrorCommandRequest mirrorCommandRequest,
                         List<String> mirrorLog,
                         LfsUpdateResult lfsUpdateResult) {
      this.lfsBlobStore = lfsBlobStoreFactory.getLfsBlobStore(repository);
      this.repository = repository;
      this.gitRepository = gitRepository;
      this.mirrorLog = mirrorLog;
      this.lfsUpdateResult = lfsUpdateResult;
      this.httpConnectionFactory = mirrorHttpConnectionProvider.createHttpConnectionFactory(mirrorCommandRequest, mirrorLog);
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
        mirrorLog.add("Failed to load lfs file:");
        mirrorLog.add(e.getMessage());
        lfsUpdateResult.increaseFailureCount();
      }
    }

    private Path loadLfsFile(LfsPointer lfsPointer) throws IOException {
      lfsUpdateResult.increaseOverallCount();
      LOG.trace("trying to load lfs file '{}' for repository {}", lfsPointer.getOid(), repository);
      mirrorLog.add(String.format("Loading lfs file with id '%s'", lfsPointer.getOid().name()));
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
}
