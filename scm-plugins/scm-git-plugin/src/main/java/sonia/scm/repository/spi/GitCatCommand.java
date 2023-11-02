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
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import javax.inject.Inject;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;


public class GitCatCommand extends AbstractGitCommand implements CatCommand {

  private static final Logger LOG = LoggerFactory.getLogger(GitCatCommand.class);

  private final LfsBlobStoreFactory lfsBlobStoreFactory;

  @Inject
  public GitCatCommand(@Assisted GitContext context, LfsBlobStoreFactory lfsBlobStoreFactory) {
    super(context);
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
  }

  @Override
  public void getCatResult(CatCommandRequest request, OutputStream output) throws IOException {
    LOG.debug("try to read content for {}", request);
    try (Loader closableObjectLoaderContainer = getLoader(request)) {
      closableObjectLoaderContainer.copyTo(output);
    }
  }

  @Override
  public InputStream getCatResultStream(CatCommandRequest request) throws IOException {
    LOG.debug("try to read content for {}", request);
    return new InputStreamWrapper(getLoader(request));
  }

  void getContent(org.eclipse.jgit.lib.Repository repo, ObjectId revId, String path, OutputStream output) throws IOException {
    try (Loader closableObjectLoaderContainer = getLoader(repo, revId, path)) {
      closableObjectLoaderContainer.copyTo(output);
    }
  }

  private Loader getLoader(CatCommandRequest request) throws IOException {
    org.eclipse.jgit.lib.Repository repo = open();
    ObjectId revId = getCommitOrDefault(repo, request.getRevision());
    if (revId == null) {
      throw notFound(entity("Revision", request.getRevision()).in(repository));
    }
    LOG.debug("loading content for file {} for revision {} in repository {}", request.getPath(), revId, repository);
    return getLoader(repo, revId, request.getPath());
  }

  private Loader getLoader(Repository repo, ObjectId revId, String path) throws IOException {
    TreeWalk treeWalk = new TreeWalk(repo);
    treeWalk.setRecursive(Util.nonNull(path).contains("/"));
    LOG.debug("load content for {} at {}", path, revId.name());
    RevWalk revWalk = new RevWalk(repo);

    RevCommit entry = null;
    try {
      entry = revWalk.parseCommit(revId);
    } catch (MissingObjectException e) {
      throw notFound(entity("Revision", revId.getName()).in(repository));
    }
    RevTree revTree = entry.getTree();

    if (revTree != null) {
      treeWalk.addTree(revTree);
    } else {
      LOG.error("could not find tree for {}", revId.name());
    }

    treeWalk.setFilter(PathFilter.create(path));

    if (treeWalk.next() && treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_BLOB) {
      Optional<LfsPointer> lfsPointer = GitUtil.getLfsPointer(repo, path, entry, treeWalk);
      if (lfsPointer.isPresent()) {
        return loadFromLfsStore(treeWalk, revWalk, lfsPointer.get());
      } else {
        return loadFromGit(repo, treeWalk, revWalk);
      }
    } else {
      throw notFound(entity("Path", path).in("Revision", revId.getName()).in(repository));
    }
  }

  private Loader loadFromGit(Repository repo, TreeWalk treeWalk, RevWalk revWalk) throws IOException {
    ObjectId blobId = treeWalk.getObjectId(0);
    ObjectLoader loader = repo.open(blobId);

    return new GitObjectLoaderWrapper(loader, treeWalk, revWalk);
  }

  private Loader loadFromLfsStore(TreeWalk treeWalk, RevWalk revWalk, LfsPointer lfsPointer) throws IOException {
    BlobStore lfsBlobStore = lfsBlobStoreFactory.getLfsBlobStore(repository);
    String oid = lfsPointer.getOid().getName();
    Blob blob = lfsBlobStore.get(oid);
    if (blob == null) {
      LOG.error("lfs blob for lob id {} not found in lfs store of repository {}", oid, repository);
      throw notFound(entity("LFS", oid).in(repository));
    }
    GitUtil.release(revWalk);
    GitUtil.release(treeWalk);
    return new BlobLoader(blob);
  }

  private interface Loader extends Closeable {
    void copyTo(OutputStream output) throws IOException;

    InputStream openStream() throws IOException;
  }

  private static class BlobLoader implements Loader {
    private final InputStream inputStream;

    private BlobLoader(Blob blob) throws IOException {
      this.inputStream = blob.getInputStream();
    }

    @Override
    public void copyTo(OutputStream output) throws IOException {
      IOUtil.copy(inputStream, output);
    }

    @Override
    public InputStream openStream() {
      return inputStream;
    }

    @Override
    public void close() throws IOException {
      this.inputStream.close();
    }
  }

  private static class GitObjectLoaderWrapper implements Loader {
    private final ObjectLoader objectLoader;
    private final TreeWalk treeWalk;
    private final RevWalk revWalk;

    private GitObjectLoaderWrapper(ObjectLoader objectLoader, TreeWalk treeWalk, RevWalk revWalk) {
      this.objectLoader = objectLoader;
      this.treeWalk = treeWalk;
      this.revWalk = revWalk;
    }

    @Override
    public void close() {
      GitUtil.release(revWalk);
      GitUtil.release(treeWalk);
    }

    public void copyTo(OutputStream output) throws IOException {
      this.objectLoader.copyTo(output);
    }

    public InputStream openStream() throws IOException {
      return objectLoader.openStream();
    }
  }

  private static class InputStreamWrapper extends FilterInputStream {

    private final Loader container;

    private InputStreamWrapper(Loader container) throws IOException {
      super(container.openStream());
      this.container = container;
    }

    @Override
    public void close() throws IOException {
      try {
        super.close();
      } finally {
        container.close();
      }
    }
  }

  public interface Factory {
    CatCommand create(GitContext context);
  }

}
