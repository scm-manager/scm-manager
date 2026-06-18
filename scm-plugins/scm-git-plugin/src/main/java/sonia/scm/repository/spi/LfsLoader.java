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
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lfs.Lfs;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.Protocol;
import org.eclipse.jgit.lfs.SmudgeFilter;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.ObjectWalk;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.store.BlobStore;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Slf4j
class LfsLoader {

  private static final Logger LOG = LoggerFactory.getLogger(LfsLoader.class);

  private final LfsBlobStoreFactory lfsBlobStoreFactory;

  @Inject
  LfsLoader(LfsBlobStoreFactory lfsBlobStoreFactory) {
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
  }

  public void loadComplete(Repository gitRepository,
                           LfsLoaderLogger mirrorLog,
                           HttpConnectionFactory httpConnectionFactory,
                           String url,
                           sonia.scm.repository.Repository repository,
                           MirrorCommandResult.LfsUpdateResult lfsUpdateResult) {
    new Worker(gitRepository, mirrorLog, httpConnectionFactory, repository, url, lfsUpdateResult)
      .loadComplete();
  }

  public void load(Repository gitRepository,
                   LfsLoaderLogger mirrorLog,
                   HttpConnectionFactory httpConnectionFactory,
                   String url,
                   sonia.scm.repository.Repository repository,
                   MirrorCommandResult.LfsUpdateResult lfsUpdateResult,
                   Collection<ObjectId> startIds,
                   Collection<ObjectId> uninterestingIds) {
    new Worker(gitRepository, mirrorLog, httpConnectionFactory, repository, url, lfsUpdateResult)
      .load(startIds, uninterestingIds);
  }

  public Collection<ObjectId> gatherAllRefs(Repository gitRepository) throws IOException {
    Collection<ObjectId> startIds = new HashSet<>();
    for (Ref ref : gitRepository.getRefDatabase().getRefs()) {
      if (ref.getObjectId() != null) {
        startIds.add(ref.getObjectId());
      }
    }
    return startIds;
  }

  private class Worker {
    private final Repository gitRepository;
    private final LfsLoaderLogger mirrorLog;
    private final HttpConnectionFactory httpConnectionFactory;
    private final sonia.scm.repository.Repository repository;
    private final MirrorCommandResult.LfsUpdateResult lfsUpdateResult;

    private Worker(
      Repository gitRepository,
      LfsLoaderLogger mirrorLog,
      HttpConnectionFactory httpConnectionFactory,
      sonia.scm.repository.Repository repository,
      String sourceUrl,
      MirrorCommandResult.LfsUpdateResult lfsUpdateResult
    ) {
      this.gitRepository = gitRepository;
      this.mirrorLog = mirrorLog;
      this.httpConnectionFactory = httpConnectionFactory;
      this.repository = repository;
      this.lfsUpdateResult = lfsUpdateResult;

      gitRepository
        .getConfig()
        .setString(ConfigConstants.CONFIG_SECTION_LFS, null, ConfigConstants.CONFIG_KEY_URL, computeLfsUrl(sourceUrl));
    }

    public void loadComplete() {
      try {
        Collection<ObjectId> startIds = gatherAllRefs(gitRepository);
        load(startIds, emptyList());
      } catch (IOException e) {
        LOG.warn("failed to gather all starting refs to load lfs files", e);
        mirrorLog.failed(e);
        lfsUpdateResult.increaseFailureCount();
      }
    }

    public void load(Collection<ObjectId> startIds, Collection<ObjectId> uninterestingIds) {
      try (ObjectWalk walk = new ObjectWalk(gitRepository)) {
        for (ObjectId id : startIds) {
          try {
            walk.markStart(walk.parseCommit(unpeelTag(walk, id).toObjectId()));
          } catch (IOException e) {
            log.warn("failed to use object id as starting ref: {}", id, e);
          }
        }
        for (ObjectId id : uninterestingIds) {
          try {
            walk.markUninteresting(walk.parseCommit(unpeelTag(walk, id).toObjectId()));
          } catch (IOException e) {
            log.warn("failed to use object id as uninteresting ref: {}", id, e);
          }
        }

        while (walk.next() != null) {
          // Do nothing, just pump the walker.
        }

        RevObject obj;
        while ((obj = walk.nextObject()) != null) {
          if (obj.getType() == Constants.OBJ_BLOB) {

            ObjectLoader loader = gitRepository.open(obj);

            if (loader.getSize() < 1024) {
              byte[] bytes = loader.getCachedBytes();
              isLfsPointer(bytes).ifPresent(this::processLfsPointer);
            }
          }
        }
      } catch (IOException e) {
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

    private ObjectId unpeelTag(RevWalk revWalk, ObjectId oldId) throws IOException {
      RevObject revObject = revWalk.parseAny(oldId);
      if (revObject instanceof RevTag) {
        return unpeelTag(revWalk, ((RevTag) revObject).getObject());
      } else {
        return revObject;
      }
    }

    private Optional<LfsPointer> isLfsPointer(byte[] bytes) throws IOException {
      try {
        return Optional.ofNullable(LfsPointer.parseLfsPointer(new ByteArrayInputStream(bytes)));
      } catch (RuntimeException e) {
        return Optional.empty();
      }
    }

    private void processLfsPointer(LfsPointer lfsPointer) {
      AnyLongObjectId oid = lfsPointer.getOid();
      BlobStore lfsBlobStore = lfsBlobStoreFactory.getLfsBlobStore(repository);
      try {
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

      return downloadLfsResource(lfs, gitRepository, httpConnectionFactory, lfsPointer);
    }

  }

  @VisibleForTesting
  void storeLfsBlob(AnyLongObjectId oid, Path tempFilePath, BlobStore lfsBlobStore) throws IOException {
    LOG.trace("temporary lfs file: {}", tempFilePath);
    Files.copy(tempFilePath, lfsBlobStore.create(oid.name()).getOutputStream());
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

  interface LfsLoaderLogger {

    void failed(Exception e);

    void loading(String name);
  }
}
