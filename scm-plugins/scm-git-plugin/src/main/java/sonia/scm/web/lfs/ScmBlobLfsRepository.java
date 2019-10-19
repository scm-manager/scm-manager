package sonia.scm.web.lfs;

import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.server.LargeFileRepository;
import org.eclipse.jgit.lfs.server.Response;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.Scope;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This LargeFileRepository is used for jGit-Servlet implementation. Under the jgit LFS Servlet hood, the
 * SCM-Repository API is used to implement the Repository.
 *
 * @since 1.54
 * Created by omilke on 03.05.2017.
 */
public class ScmBlobLfsRepository implements LargeFileRepository {

  private final BlobStore blobStore;
  private final AccessTokenBuilderFactory tokenBuilderFactory;

  /**
   * This URI is used to determine the actual URI for Upload / Download. Must be full URI (or rewritable by reverse
   * proxy).
   */
  private final String baseUri;
  private final Repository repository;

  private AccessToken accessToken;

  /**
   * Creates a {@link ScmBlobLfsRepository} for the provided repository.
   *
   * @param repository          The current scm repository this LFS repository is used for.
   * @param blobStore           The SCM Blobstore used for this @{@link LargeFileRepository}.
   * @param tokenBuilderFactory The token builder used to create short lived access tokens.
   * @param baseUri             This URI is used to determine the actual URI for Upload / Download. Must be full URI (or
   */

  public ScmBlobLfsRepository(Repository repository, BlobStore blobStore, AccessTokenBuilderFactory tokenBuilderFactory, String baseUri) {
    this.repository = repository;
    this.blobStore = blobStore;
    this.tokenBuilderFactory = tokenBuilderFactory;
    this.baseUri = baseUri;
  }

  @Override
  public Response.Action getDownloadAction(AnyLongObjectId id) {

    return getAction(id, Scope.valueOf(RepositoryPermissions.read(repository).asShiroString(), RepositoryPermissions.pull(repository).asShiroString()));
  }

  @Override
  public Response.Action getUploadAction(AnyLongObjectId id, long size) {

    return getAction(id, Scope.valueOf(RepositoryPermissions.read(repository).asShiroString(), RepositoryPermissions.pull(repository).asShiroString(), RepositoryPermissions.push(repository).asShiroString()));
  }

  @Override
  public Response.Action getVerifyAction(AnyLongObjectId id) {

    //validation is optional. We do not support it.
    return null;
  }

  @Override
  public long getSize(AnyLongObjectId id) throws IOException {

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
  private Response.Action getAction(AnyLongObjectId id, Scope scope) {

    //LFS protocol has to provide the information on where to put or get the actual content, i. e.
    //the actual URI for up- and download.

    return new ExpiringAction(baseUri + id.getName(), getAccessToken(scope));
  }

  private AccessToken getAccessToken(Scope scope) {
    if (accessToken == null) {
      accessToken = tokenBuilderFactory
        .create()
        .expiresIn(5, TimeUnit.MINUTES)
        .scope(scope)
        .build();
    }
    return accessToken;
  }

}
