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

package sonia.scm.web.lfs;

import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.server.LargeFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.security.AccessToken;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;

/**
 * This LargeFileRepository is used for jGit-Servlet implementation. Under the jgit LFS Servlet hood, the
 * SCM-Repository API is used to implement the Repository.
 *
 * @since 1.54
 * Created by omilke on 03.05.2017.
 */
public class ScmBlobLfsRepository implements LargeFileRepository {

  private static final Logger LOG = LoggerFactory.getLogger(ScmBlobLfsRepository.class);

  private final BlobStore blobStore;
  private final LfsAccessTokenFactory tokenFactory;

  /**
   * This URI is used to determine the actual URI for Upload / Download. Must be full URI (or rewritable by reverse
   * proxy).
   */
  private final String baseUri;
  private final Repository repository;

  /**
   * A {@link ScmBlobLfsRepository} is created for either download or upload, not both. Therefore we can cache the
   * access token and do not have to create them anew for each action.
   */
  private AccessToken accessToken;

  /**
   * Creates a {@link ScmBlobLfsRepository} for the provided repository.
   *
   * @param repository          The current scm repository this LFS repository is used for.
   * @param blobStore           The SCM Blobstore used for this @{@link LargeFileRepository}.
   * @param tokenFactory        The token builder for subsequent LFS requests.
   * @param baseUri             This URI is used to determine the actual URI for Upload / Download. Must be full URI (or
   */

  public ScmBlobLfsRepository(Repository repository, BlobStore blobStore, LfsAccessTokenFactory tokenFactory, String baseUri) {
    this.repository = repository;
    this.blobStore = blobStore;
    this.tokenFactory = tokenFactory;
    this.baseUri = baseUri;
  }

  @Override
  public ExpiringAction getDownloadAction(AnyLongObjectId id) {
    if (accessToken == null) {
      LOG.trace("create access token to download lfs object {} from repository {}", id, repository);
      accessToken = tokenFactory.createReadAccessToken(repository);
    }
    return getAction(id, accessToken);
  }

  @Override
  public ExpiringAction getUploadAction(AnyLongObjectId id, long size) {
    if (accessToken == null) {
      LOG.trace("create access token to upload lfs object {} to repository {}", id, repository);
      accessToken = tokenFactory.createWriteAccessToken(repository);
    }
    return getAction(id, accessToken);
  }

  @Override
  public ExpiringAction getVerifyAction(AnyLongObjectId id) {

    //validation is optional. We do not support it.
    return null;
  }

  @Override
  public long getSize(AnyLongObjectId id) {

    //this needs to be size of what is will be written into the response of the download. Clients are likely to
    // verify it.
    Blob blob = this.blobStore.get(id.getName());
    if (blob == null) {

      return -1;
    } else {

      return blob.getSize();
    }

  }

  /**
   * Constructs the Download / Upload actions to be supplied to the client.
   */
  private ExpiringAction getAction(AnyLongObjectId id, AccessToken token) {

    //LFS protocol has to provide the information on where to put or get the actual content, i. e.
    //the actual URI for up- and download.

    return new ExpiringAction(baseUri + id.getName(), token);
  }
}
