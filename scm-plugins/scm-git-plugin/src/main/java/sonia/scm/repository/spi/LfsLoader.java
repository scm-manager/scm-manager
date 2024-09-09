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

import com.google.common.annotations.VisibleForTesting;
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
import java.util.HashSet;
import java.util.Set;

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
    inspectTree(newObjectId, gitRepository, mirrorLog, lfsUpdateResult, repository, httpConnectionFactory, url, new HashSet<>(1000));
  }

  void inspectTree(ObjectId newObjectId,
                   Repository gitRepository,
                   LfsLoaderLogger mirrorLog,
                   LfsUpdateResult lfsUpdateResult,
                   sonia.scm.repository.Repository repository,
                   HttpConnectionFactory httpConnectionFactory,
                   String url,
                   Set<ObjectId> alreadyVisited) {
    EntryHandler entryHandler = createEntryHandler(gitRepository, mirrorLog, lfsUpdateResult, repository, httpConnectionFactory);
    inspectTree(newObjectId, entryHandler, gitRepository, mirrorLog, lfsUpdateResult, url, alreadyVisited);
  }

  @VisibleForTesting
  EntryHandler createEntryHandler(Repository gitRepository, LfsLoaderLogger mirrorLog, LfsUpdateResult lfsUpdateResult, sonia.scm.repository.Repository repository, HttpConnectionFactory httpConnectionFactory) {
    return new EntryHandler(repository, gitRepository, mirrorLog, lfsUpdateResult, httpConnectionFactory);
  }

  private void inspectTree(ObjectId newObjectId,
                           EntryHandler entryHandler,
                           Repository gitRepository,
                           LfsLoaderLogger mirrorLog,
                           LfsUpdateResult lfsUpdateResult,
                           String sourceUrl,
                           Set<ObjectId> alreadyVisited) {
    try {
      gitRepository
        .getConfig()
        .setString(ConfigConstants.CONFIG_SECTION_LFS, null, ConfigConstants.CONFIG_KEY_URL, computeLfsUrl(sourceUrl));

      TreeWalk treeWalk = new TreeWalk(gitRepository);
      treeWalk.setFilter(new FilteringScmLfsPointerFilter(alreadyVisited));
      treeWalk.setRecursive(true);

      RevWalk revWalk = new RevWalk(gitRepository);
      revWalk.markStart(revWalk.parseCommit(newObjectId));

      for (RevCommit commit : revWalk) {
        if (!alreadyVisited.add(commit.toObjectId())) {
          LOG.trace("skipping commit {}", commit);
          break;
        }
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

  @VisibleForTesting
  Path downloadLfsResource(Lfs lfs, Repository gitRepository, HttpConnectionFactory connectionFactory, LfsPointer lfsPointer) throws IOException {
    return SmudgeFilter.downloadLfsResource(
      lfs,
      gitRepository,
      connectionFactory,
      lfsPointer
    )
      .iterator()
      .next();
  }

  @VisibleForTesting
  void storeLfsBlob(AnyLongObjectId oid, Path tempFilePath, BlobStore lfsBlobStore) throws IOException {
    LOG.trace("temporary lfs file: {}", tempFilePath);
    Files.copy(
      tempFilePath,
      lfsBlobStore
        .create(oid.name())
        .getOutputStream()
    );
  }

  @VisibleForTesting
  class EntryHandler {

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

    @VisibleForTesting
    void handleTreeEntry(TreeWalk treeWalk) {
      try (InputStream is = gitRepository.open(treeWalk.getObjectId(0), Constants.OBJ_BLOB).openStream()) {
        LfsPointer lfsPointer = LfsPointer.parseLfsPointer(is);
        AnyLongObjectId oid = lfsPointer.getOid();

        if (lfsBlobStore.get(oid.name()) == null) {
          Path tempFilePath = loadLfsFile(lfsPointer);
          try {
            storeLfsBlob(oid, tempFilePath, lfsBlobStore);
          } finally {
            Files.delete(tempFilePath);
          }
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

      return downloadLfsResource(
        lfs,
        gitRepository,
        httpConnectionFactory,
        lfsPointer
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

  private static class FilteringScmLfsPointerFilter extends ScmLfsPointerFilter {

    private final Set<ObjectId> alreadyVisited;

    private FilteringScmLfsPointerFilter(Set<ObjectId> alreadyVisited) {
      this.alreadyVisited = alreadyVisited;
    }

    @Override
    public boolean include(TreeWalk walk) throws IOException {
      if (!alreadyVisited.add(walk.getObjectId(0))) {
        LOG.trace("skipping object {}", walk.getObjectId(0));
        return false;
      }
      return super.include(walk);
    }
  }
}
