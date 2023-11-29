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

package sonia.scm.web.lfs.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jgit.lfs.server.LargeFileRepository;
import org.eclipse.jgit.lfs.server.LfsProtocolServlet;
import org.eclipse.jgit.lfs.server.fs.FileLfsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.GitFileLockStoreFactory;
import sonia.scm.store.BlobStore;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.LfsLockingProtocolServlet;
import sonia.scm.web.lfs.LfsAccessTokenFactory;
import sonia.scm.web.lfs.LfsBlobStoreFactory;
import sonia.scm.web.lfs.ScmBlobLfsRepository;

/**
 * This factory class is a helper class to provide the {@link LfsProtocolServlet} and the {@link FileLfsServlet}
 * belonging to a SCM Repository.
 *
 * @since 1.54
 * Created by omilke on 11.05.2017.
 */
@Singleton
public class LfsServletFactory {

  private static final Logger LOG = LoggerFactory.getLogger(LfsServletFactory.class);

  private final LfsBlobStoreFactory lfsBlobStoreFactory;
  private final LfsAccessTokenFactory tokenFactory;
  private final GitFileLockStoreFactory lockStoreFactory;
  private final UserDisplayManager userDisplayManager;
  private final ObjectMapper objectMapper;

  @Inject
  public LfsServletFactory(LfsBlobStoreFactory lfsBlobStoreFactory, LfsAccessTokenFactory tokenFactory, GitFileLockStoreFactory lockStoreFactory, UserDisplayManager userDisplayManager, ObjectMapper objectMapper) {
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
    this.tokenFactory = tokenFactory;
    this.lockStoreFactory = lockStoreFactory;
    this.userDisplayManager = userDisplayManager;
    this.objectMapper = objectMapper;
  }

  /**
   * Builds the {@link LfsProtocolServlet} (jgit API) for a SCM Repository.
   *
   * @param repository The SCM Repository to build the servlet for.
   * @param request    The {@link HttpServletRequest} the used to access the SCM Repository.
   * @return The {@link LfsProtocolServlet} to provide the LFS Batch API for a SCM Repository.
   */
  public LfsProtocolServlet createProtocolServletFor(Repository repository, HttpServletRequest request) {
    LOG.trace("create lfs protocol servlet for repository {}", repository);
    BlobStore blobStore = lfsBlobStoreFactory.getLfsBlobStore(repository);
    String baseUri = buildBaseUri(repository, request);

    LargeFileRepository largeFileRepository = new ScmBlobLfsRepository(repository, blobStore, tokenFactory, baseUri);
    return new ScmLfsProtocolServlet(largeFileRepository);
  }

  /**
   * Builds the {@link FileLfsServlet} (jgit API) for a SCM Repository.
   *
   * @param repository The SCM Repository to build the servlet for.
   * @param request    The {@link HttpServletRequest} the used to access the SCM Repository.
   * @return The {@link FileLfsServlet} to provide the LFS Upload / Download API for a SCM Repository.
   */
  public HttpServlet createFileLfsServletFor(Repository repository, HttpServletRequest request) {
    LOG.trace("create lfs file servlet for repository {}", repository);
    return new ScmFileTransferServlet(lfsBlobStoreFactory.getLfsBlobStore(repository));
  }

  public LfsLockingProtocolServlet createLockServletFor(Repository repository) {
    LOG.trace("create lfs lock servlet for repository {}", repository);
    return new LfsLockingProtocolServlet(repository, lockStoreFactory.create(repository), userDisplayManager, objectMapper);
  }

  /**
   * Build the complete URI, under which the File Transfer API for this repository will be will be reachable.
   *
   * @param repository The repository to build the File Transfer URI for.
   * @param request    The request to construct the complete URI from.
   */
  @VisibleForTesting
  static String buildBaseUri(Repository repository, HttpServletRequest request) {
    return String.format("%s/repo/%s/%s.git/info/lfs/objects/", HttpUtil.getCompleteUrl(request), repository.getNamespace(), repository.getName());
  }

}
