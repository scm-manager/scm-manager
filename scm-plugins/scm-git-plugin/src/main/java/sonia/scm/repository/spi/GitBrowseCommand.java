/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.eclipse.jgit.attributes.Attributes;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.LfsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.GitSubModuleParser;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SubRepository;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.util.Util;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;
import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.ASYNCHRONOUS;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class GitBrowseCommand extends AbstractGitCommand
  implements BrowseCommand
{

  /** Field description */
  public static final String PATH_MODULES = ".gitmodules";

  /**
   * the logger for GitBrowseCommand
   */
  private static final Logger logger = LoggerFactory.getLogger(GitBrowseCommand.class);

  /** sub repository cache */
  private final Map<ObjectId, Map<String, SubRepository>> subrepositoryCache = Maps.newHashMap();

  private final Object asyncMonitor = new Object();

  private final LfsBlobStoreFactory lfsBlobStoreFactory;

  private final SyncAsyncExecutor executor;

  private BrowserResult browserResult;

  private int resultCount = 0;

  public GitBrowseCommand(GitContext context, Repository repository, LfsBlobStoreFactory lfsBlobStoreFactory, SyncAsyncExecutor executor) {
    super(context, repository);
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
    this.executor = executor;
  }

  @Override
  public BrowserResult getBrowserResult(BrowseCommandRequest request)
    throws IOException {
    logger.debug("try to create browse result for {}", request);

    org.eclipse.jgit.lib.Repository repo = open();
    ObjectId revId = computeRevIdToBrowse(request, repo);

    if (revId != null) {
      browserResult = new BrowserResult(revId.getName(), request.getRevision(), getEntry(repo, request, revId));
      return browserResult;
    } else {
      logger.warn("could not find head of repository {}, empty?", repository.getNamespaceAndName());
      return new BrowserResult(Constants.HEAD, request.getRevision(), createEmptyRoot());
    }
  }

  private ObjectId computeRevIdToBrowse(BrowseCommandRequest request, org.eclipse.jgit.lib.Repository repo) throws IOException {
    if (Util.isEmpty(request.getRevision())) {
      return getDefaultBranch(repo);
    } else {
      ObjectId revId = GitUtil.getRevisionId(repo, request.getRevision());
      if (revId == null) {
        logger.error("could not find revision {}", request.getRevision());
        throw notFound(entity("Revision", request.getRevision()).in(this.repository));
      }
      return revId;
    }
  }

  private FileObject createEmptyRoot() {
    FileObject fileObject = new FileObject();
    fileObject.setName("");
    fileObject.setPath("");
    fileObject.setDirectory(true);
    fileObject.setTruncated(false);
    return fileObject;
  }

  private FileObject createFileObject(org.eclipse.jgit.lib.Repository repo,
                                      BrowseCommandRequest request, ObjectId revId, TreeEntry treeEntry)
    throws IOException {

    FileObject file = new FileObject();

    String path = treeEntry.getPathString();

    file.setName(treeEntry.getNameString());
    file.setPath(path);

    SubRepository sub = null;

    if (!request.isDisableSubRepositoryDetection())
    {
      sub = getSubRepository(repo, revId, path);
    }

    if (sub != null)
    {
      logger.trace("{} seems to be a sub repository", path);
      file.setDirectory(true);
      file.setSubRepository(sub);
    }
    else
    {
      ObjectLoader loader = repo.open(treeEntry.getObjectId());

      file.setDirectory(loader.getType() == Constants.OBJ_TREE);

      // don't show message and date for directories to improve performance
      if (!file.isDirectory() &&!request.isDisableLastCommit())
      {
        file.setPartialResult(true);
        RevCommit commit;
        try (RevWalk walk = new RevWalk(repo)) {
          commit = walk.parseCommit(revId);
        }
        Optional<LfsPointer> lfsPointer = getLfsPointer(repo, path, commit, treeEntry);

        if (lfsPointer.isPresent()) {
          setFileLengthFromLfsBlob(lfsPointer.get(), file);
        } else {
          file.setLength(loader.getSize());
        }

        executor.execute(
          new CompleteFileInformation(path, revId, repo, file, request),
          new AbortFileInformation(request)
        );
      }
    }
    return file;
  }

  private void updateCache(BrowseCommandRequest request) {
    request.updateCache(browserResult);
    logger.info("updated browser result for repository {}", repository.getNamespaceAndName());
  }

  private FileObject getEntry(org.eclipse.jgit.lib.Repository repo, BrowseCommandRequest request, ObjectId revId) throws IOException {
    try (RevWalk revWalk = new RevWalk(repo); TreeWalk treeWalk = new TreeWalk(repo)) {
      logger.debug("load repository browser for revision {}", revId.name());

      if (!isRootRequest(request)) {
        treeWalk.setFilter(PathFilter.create(request.getPath()));
      }

      RevTree tree = revWalk.parseTree(revId);

      if (tree != null) {
        treeWalk.addTree(tree);
      } else {
        throw new IllegalStateException("could not find tree for " + revId.name());
      }

      if (isRootRequest(request)) {
        FileObject result = createEmptyRoot();
        findChildren(result, repo, request, revId, treeWalk);
        return result;
      } else {
        FileObject result = findFirstMatch(repo, request, revId, treeWalk);
        if ( result.isDirectory() ) {
          treeWalk.enterSubtree();
          findChildren(result, repo, request, revId, treeWalk);
        }
        return result;
      }
    }
  }

  private boolean isRootRequest(BrowseCommandRequest request) {
    return Strings.isNullOrEmpty(request.getPath()) || "/".equals(request.getPath());
  }

  private void findChildren(FileObject parent, org.eclipse.jgit.lib.Repository repo, BrowseCommandRequest request, ObjectId revId, TreeWalk treeWalk) throws IOException {
    TreeEntry entry = new TreeEntry();
    createTree(parent.getPath(), entry, repo, request, treeWalk);
    convertToFileObject(parent, repo, request, revId, entry.getChildren());
  }

  private void convertToFileObject(FileObject parent, org.eclipse.jgit.lib.Repository repo, BrowseCommandRequest request, ObjectId revId, List<TreeEntry> entries) throws IOException {
    List<FileObject> files = Lists.newArrayList();
    Iterator<TreeEntry> entryIterator = entries.iterator();
    boolean hasNext;
    while ((hasNext = entryIterator.hasNext()) && resultCount < request.getLimit() + request.getOffset())
    {
      TreeEntry entry = entryIterator.next();
      FileObject fileObject = createFileObject(repo, request, revId, entry);

      if (!fileObject.isDirectory()) {
        ++resultCount;
      }

      if (request.isRecursive() && fileObject.isDirectory()) {
        convertToFileObject(fileObject, repo, request, revId, entry.getChildren());
      }

      if (resultCount > request.getOffset() || fileObject.isDirectory()) {
        files.add(fileObject);
      }
    }

    parent.setChildren(files);

    parent.setTruncated(hasNext);
  }

  private Optional<TreeEntry> createTree(String path, TreeEntry parent, org.eclipse.jgit.lib.Repository repo, BrowseCommandRequest request, TreeWalk treeWalk) throws IOException {
    List<TreeEntry> entries = new ArrayList<>();
    while (treeWalk.next()) {
      TreeEntry treeEntry = new TreeEntry(repo, treeWalk);
      if (!treeEntry.getPathString().startsWith(path)) {
        parent.setChildren(entries);
        return of(treeEntry);
      }

      entries.add(treeEntry);

      if (request.isRecursive() && treeEntry.isDirectory()) {
        treeWalk.enterSubtree();
        Optional<TreeEntry> surplus = createTree(treeEntry.getNameString(), treeEntry, repo, request, treeWalk);
        surplus.ifPresent(entries::add);
      }
    }
    parent.setChildren(entries);
    return empty();
  }

  private FileObject findFirstMatch(org.eclipse.jgit.lib.Repository repo,
                                    BrowseCommandRequest request, ObjectId revId, TreeWalk treeWalk) throws IOException {
    String[] pathElements = request.getPath().split("/");
    int currentDepth = 0;
    int limit = pathElements.length;

    while (treeWalk.next()) {
      String name = treeWalk.getNameString();

      if (name.equalsIgnoreCase(pathElements[currentDepth])) {
        currentDepth++;

        if (currentDepth >= limit) {
          return createFileObject(repo, request, revId, new TreeEntry(repo, treeWalk));
        } else {
          treeWalk.enterSubtree();
        }
      }
    }

    throw notFound(entity("File", request.getPath()).in("Revision", revId.getName()).in(this.repository));
  }

  private Map<String, SubRepository> getSubRepositories(org.eclipse.jgit.lib.Repository repo, ObjectId revision)
    throws IOException {

    logger.debug("read submodules of {} at {}", repository.getName(), revision);

    try ( ByteArrayOutputStream baos = new ByteArrayOutputStream() ) {
      new GitCatCommand(context, repository, lfsBlobStoreFactory).getContent(repo, revision,
        PATH_MODULES, baos);
      return GitSubModuleParser.parse(baos.toString());
    } catch (NotFoundException ex) {
      logger.trace("could not find .gitmodules: {}", ex.getMessage());
      return Collections.emptyMap();
    }
  }

  private SubRepository getSubRepository(org.eclipse.jgit.lib.Repository repo, ObjectId revId, String path)
    throws IOException {
    Map<String, SubRepository> subRepositories = subrepositoryCache.get(revId);

    if (subRepositories == null) {
      subRepositories = getSubRepositories(repo, revId);
      subrepositoryCache.put(revId, subRepositories);
    }

    if (subRepositories != null) {
      return subRepositories.get(path);
    }
    return null;
  }

  private Optional<LfsPointer> getLfsPointer(org.eclipse.jgit.lib.Repository repo, String path, RevCommit commit, TreeEntry treeWalk) {
    try {
      Attributes attributes = LfsFactory.getAttributesForPath(repo, path, commit);

      return GitUtil.getLfsPointer(repo, treeWalk.getObjectId(), attributes);
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "could not read lfs pointer", e);
    }
  }

  private void setFileLengthFromLfsBlob(LfsPointer lfsPointer, FileObject file) {
    BlobStore lfsBlobStore = lfsBlobStoreFactory.getLfsBlobStore(repository);
    String oid = lfsPointer.getOid().getName();
    Blob blob = lfsBlobStore.get(oid);
    if (blob == null) {
      logger.error("lfs blob for lob id {} not found in lfs store of repository {}", oid, repository.getNamespaceAndName());
      file.setLength(null);
    } else {
      file.setLength(blob.getSize());
    }
  }

  private class CompleteFileInformation implements Consumer<SyncAsyncExecutor.ExecutionType> {
    private final String path;
    private final ObjectId revId;
    private final org.eclipse.jgit.lib.Repository repo;
    private final FileObject file;
    private final BrowseCommandRequest request;

    public CompleteFileInformation(String path, ObjectId revId, org.eclipse.jgit.lib.Repository repo, FileObject file, BrowseCommandRequest request) {
      this.path = path;
      this.revId = revId;
      this.repo = repo;
      this.file = file;
      this.request = request;
    }

    @Override
    public void accept(SyncAsyncExecutor.ExecutionType executionType) {
      logger.trace("fetch last commit for {} at {}", path, revId.getName());

      Stopwatch sw = Stopwatch.createStarted();

      Optional<RevCommit> commit = getLatestCommit(repo, revId, path);

      synchronized (asyncMonitor) {
        file.setPartialResult(false);
        if (commit.isPresent()) {
          applyValuesFromCommit(executionType, commit.get());
        } else {
          logger.warn("could not find latest commit for {} on {}", path, revId);
        }
      }

      logger.trace("finished loading of last commit {} of {} in {}", revId.getName(), path, sw.stop());
    }

    private Optional<RevCommit> getLatestCommit(org.eclipse.jgit.lib.Repository repo,
                                      ObjectId revId, String path) {
      try (RevWalk walk = new RevWalk(repo)) {
        walk.setTreeFilter(AndTreeFilter.create(TreeFilter.ANY_DIFF, PathFilter.create(path)));

        RevCommit commit = walk.parseCommit(revId);

        walk.markStart(commit);
        return of(Util.getFirst(walk));
      } catch (IOException ex) {
        logger.error("could not parse commit for file", ex);
        return empty();
      }
    }

    private void applyValuesFromCommit(SyncAsyncExecutor.ExecutionType executionType, RevCommit commit) {
      file.setCommitDate(GitUtil.getCommitTime(commit));
      file.setDescription(commit.getShortMessage());
      if (executionType == ASYNCHRONOUS && browserResult != null) {
        updateCache(request);
      }
    }
  }

  private class AbortFileInformation implements Runnable {
    private final BrowseCommandRequest request;

    public AbortFileInformation(BrowseCommandRequest request) {
      this.request = request;
    }

    @Override
    public void run() {
      synchronized (asyncMonitor) {
        if (markPartialAsAborted(browserResult.getFile())) {
          updateCache(request);
        }
      }
    }

    private boolean markPartialAsAborted(FileObject file) {
      boolean changed = false;
      if (file.isPartialResult()) {
        file.setPartialResult(false);
        file.setComputationAborted(true);
        changed = true;
      }
      for (FileObject child : file.getChildren()) {
        changed |= markPartialAsAborted(child);
      }
      return changed;
    }
  }

  private class TreeEntry {

    private final String pathString;
    private final String nameString;
    private final ObjectId objectId;
    private final boolean directory;
    private List<TreeEntry> children = emptyList();

    TreeEntry() {
      pathString = "";
      nameString = "";
      objectId = null;
      directory = true;
    }

    TreeEntry(org.eclipse.jgit.lib.Repository repo, TreeWalk treeWalk) throws IOException {
      this.pathString = treeWalk.getPathString();
      this.nameString = treeWalk.getNameString();
      this.objectId = treeWalk.getObjectId(0);
      ObjectLoader loader = repo.open(objectId);

      this.directory = loader.getType() == Constants.OBJ_TREE;
    }

    String getPathString() {
      return pathString;
    }

    String getNameString() {
      return nameString;
    }

    ObjectId getObjectId() {
      return objectId;
    }

    boolean isDirectory() {
      return directory;
    }

    List<TreeEntry> getChildren() {
      return children;
    }

    void setChildren(List<TreeEntry> children) {
      sort(children, TreeEntry::isDirectory, TreeEntry::getNameString);
      this.children = children;
    }
  }
}
