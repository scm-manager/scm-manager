package sonia.scm.web.lfs.servlet;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.jgit.lfs.server.LargeFileRepository;
import org.eclipse.jgit.lfs.server.LfsProtocolServlet;
import org.eclipse.jgit.lfs.server.fs.FileLfsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.lfs.ScmBlobLfsRepository;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * This factory class is a helper class to provide the {@link LfsProtocolServlet} and the {@link FileLfsServlet}
 * belonging to a SCM Repository.
 *
 * @since 1.54
 * Created by omilke on 11.05.2017.
 */
public class LfsServletFactory {

  private static final String GIT_LFS_REPOSITORY_POSTFIX = "-git-lfs";

  private static final Logger logger = LoggerFactory.getLogger(LfsServletFactory.class);

  private final BlobStoreFactory blobStoreFactory;

  @Inject
  public LfsServletFactory(BlobStoreFactory blobStoreFactory) {

    this.blobStoreFactory = blobStoreFactory;
  }

  /**
   * Builds the {@link LfsProtocolServlet} (jgit API) for a SCM Repository.
   *
   * @param repository The SCM Repository to build the servlet for.
   * @param request    The {@link HttpServletRequest} the used to access the SCM Repository.
   * @return The {@link LfsProtocolServlet} to provide the LFS Batch API for a SCM Repository.
   */
  public LfsProtocolServlet createProtocolServletFor(Repository repository, HttpServletRequest request) {

    BlobStore blobStore = getBlobStore(repository);
    String baseUri = buildBaseUri(repository, request);

    LargeFileRepository largeFileRepository = new ScmBlobLfsRepository(blobStore, baseUri);
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

    return new ScmFileTransferServlet(getBlobStore(repository));
  }

  /**
   * Build the complete URI, under which the File Transfer API for this repository will be will be reachable.
   *
   * @param repository The repository to build the File Transfer URI for.
   * @param request    The request to construct the complete URI from.
   */
  @VisibleForTesting
  static String buildBaseUri(Repository repository, HttpServletRequest request) {

    return String.format("%s/git/%s.git/info/lfs/objects/", HttpUtil.getCompleteUrl(request), repository.getName());
  }

  /**
   * Provides a {@link BlobStore} corresponding to the SCM Repository.
   * <p>
   * git-lfs repositories should generally carry the same name as their regular SCM repository counterparts. However,
   * we have decided to store them under their IDs instead of their names, since the names might change and provide
   * other drawbacks, as well.
   * <p>
   * These repositories will have {@linkplain #GIT_LFS_REPOSITORY_POSTFIX} appended to their IDs.
   *
   * @param repository The SCM Repository to provide a LFS {@link BlobStore} for.
   */
  @VisibleForTesting
  BlobStore getBlobStore(Repository repository) {

    return blobStoreFactory.getBlobStore(repository.getId() + GIT_LFS_REPOSITORY_POSTFIX);
  }


}
