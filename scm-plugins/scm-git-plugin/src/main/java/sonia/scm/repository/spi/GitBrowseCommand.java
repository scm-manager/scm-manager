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

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
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
import sonia.scm.repository.SubRepository;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.util.Util;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;
import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.ASYNCHRONOUS;

/**
 * @author Sebastian Sdorra
 */
public class GitBrowseCommand extends AbstractGitCommand
  implements BrowseCommand {

  /**
   * Field description
   */
  public static final String PATH_MODULES = ".gitmodules";

  /**
   * the logger for GitBrowseCommand
   */
  private static final Logger logger = LoggerFactory.getLogger(GitBrowseCommand.class);

  /**
   * sub repository cache
   */
  private final Map<ObjectId, Map<String, SubRepository>> subrepositoryCache = Maps.newHashMap();

  private final Object asyncMonitor = new Object();

  private final LfsBlobStoreFactory lfsBlobStoreFactory;

  private final SyncAsyncExecutor executor;

  private BrowserResult browserResult;

  private BrowseCommandRequest request;

  private org.eclipse.jgit.lib.Repository repo;

  private ObjectId revId;

  private int resultCount = 0;

  @Inject
  public GitBrowseCommand(@Assisted GitContext context, LfsBlobStoreFactory lfsBlobStoreFactory, SyncAsyncExecutorProvider executorProvider) {
    this(context, lfsBlobStoreFactory, executorProvider.createExecutorWithDefaultTimeout());
  }

  public GitBrowseCommand(GitContext context, LfsBlobStoreFactory lfsBlobStoreFactory, SyncAsyncExecutor executor) {
    super(context);
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
    this.executor = executor;
  }

  @Override
  public BrowserResult getBrowserResult(BrowseCommandRequest request)
    throws IOException {
    logger.debug("try to create browse result for {}", request);

    resultCount = 0;

    this.request = request;
    repo = open();
    revId = computeRevIdToBrowse();

    if (revId != null) {
      browserResult = new BrowserResult(revId.getName(), request.getRevision(), getEntry());
      return browserResult;
    } else {
      logger.warn("could not find head of repository {}, empty?", repository);
      return new BrowserResult(Constants.HEAD, request.getRevision(), createEmptyRoot());
    }
  }

  private ObjectId computeRevIdToBrowse() throws IOException {
    if (Util.isEmpty(request.getRevision())) {
      return getDefaultBranch(repo);
    } else {
      ObjectId revisionId = GitUtil.getRevisionId(repo, request.getRevision());
      if (revisionId == null) {
        logger.error("could not find revision {}", request.getRevision());
        throw notFound(entity("Revision", request.getRevision()).in(this.repository));
      }
      return revisionId;
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

  private FileObject createFileObject(TreeEntry treeEntry) throws IOException {

    FileObject file = new FileObject();

    String path = treeEntry.getPathString();

    file.setName(treeEntry.getNameString());
    file.setPath(path);

    if (treeEntry.getType() == TreeType.SUB_REPOSITORY) {
      logger.trace("{} seems to be a sub repository", path);
      file.setDirectory(true);
      file.setSubRepository(treeEntry.subRepository);
    } else {
      ObjectLoader loader = repo.open(treeEntry.getObjectId());

      file.setDirectory(loader.getType() == Constants.OBJ_TREE);

      // don't show message and date for directories to improve performance
      if (!file.isDirectory() && !request.isDisableLastCommit()) {
        file.setPartialResult(true);
        RevCommit commit;
        try (RevWalk walk = new RevWalk(repo)) {
          commit = walk.parseCommit(revId);
        }
        Optional<LfsPointer> lfsPointer = getLfsPointer(path, commit, treeEntry);

        if (lfsPointer.isPresent()) {
          setFileLengthFromLfsBlob(lfsPointer.get(), file);
        } else {
          file.setLength(loader.getSize());
        }

        executor.execute(
          new CompleteFileInformation(path, file),
          new AbortFileInformation()
        );
      }
    }
    return file;
  }

  private void updateCache() {
    request.updateCache(browserResult);
    logger.info("updated browser result for repository {}", repository);
  }

  private FileObject getEntry() throws IOException {
    try (RevWalk revWalk = new RevWalk(repo); TreeWalk treeWalk = new TreeWalk(repo)) {
      if (logger.isDebugEnabled()) { // method call in logger call
        logger.debug("load repository browser for revision {}", revId.name());
      }

      if (!isRootRequest()) {
        treeWalk.setFilter(PathFilter.create(request.getPath()));
      }

      RevTree tree = revWalk.parseTree(revId);

      treeWalk.addTree(tree);

      if (isRootRequest()) {
        FileObject result = createEmptyRoot();
        findChildren(result, treeWalk);
        return result;
      } else {
        FileObject result = findFirstMatch(treeWalk);
        if (result.isDirectory()) {
          treeWalk.enterSubtree();
          findChildren(result, treeWalk);
        }
        return result;
      }
    }
  }

  private boolean isRootRequest() {
    return Strings.isNullOrEmpty(request.getPath()) || "/".equals(request.getPath());
  }

  private void findChildren(FileObject parent, TreeWalk treeWalk) throws IOException {
    TreeEntry entry = new TreeEntry();
    createTree(entry, treeWalk);
    convertToFileObject(parent, entry.getChildren());
  }

  private void convertToFileObject(FileObject parent, List<TreeEntry> entries) throws IOException {
    List<FileObject> files = Lists.newArrayList();
    Iterator<TreeEntry> entryIterator = entries.iterator();
    boolean hasNext;
    while ((hasNext = entryIterator.hasNext()) && resultCount < request.getLimit() + request.getOffset()) {
      TreeEntry entry = entryIterator.next();
      FileObject fileObject = createFileObject(entry);

      if (!fileObject.isDirectory()) {
        ++resultCount;
      }

      if (request.isRecursive() && fileObject.isDirectory()) {
        convertToFileObject(fileObject, entry.getChildren());
      }

      if (resultCount > request.getOffset() || (request.getOffset() == 0 && fileObject.isDirectory())) {
        files.add(fileObject);
      }
    }

    parent.setChildren(files);

    parent.setTruncated(hasNext);
  }

  private void createTree(TreeEntry parent, TreeWalk treeWalk) throws IOException {
    Deque<TreeEntry> parents = new ArrayDeque<>();
    parents.push(parent);
    while (treeWalk.next()) {
      final String currentPath = treeWalk.getPathString();
      while (!currentPath.startsWith(appendTrailingSlashIfNeeded(parents))) {
        parents.pop();
      }
      TreeEntry currentParent = parents.peek();
      TreeEntry treeEntry = createTreeEntry(repo, treeWalk);
      if (treeEntry != null) {
        currentParent.addChild(treeEntry);
        if (request.isRecursive() && treeEntry.getType() == TreeType.DIRECTORY) {
          treeWalk.enterSubtree();
          parents.push(treeEntry);
        }
      } else {
        logger.warn("failed to find tree entry for {}", currentPath);
      }
    }
  }

  private String appendTrailingSlashIfNeeded(Deque<TreeEntry> parents) {
    String path = parents.peek().pathString;
    return path.length() == 0 ? path : path + "/";
  }

  private FileObject findFirstMatch(TreeWalk treeWalk) throws IOException {
    String[] pathElements = request.getPath().split("/");
    int currentDepth = 0;
    int limit = pathElements.length;

    while (treeWalk.next()) {
      String name = treeWalk.getNameString();

      if (name.equalsIgnoreCase(pathElements[currentDepth])) {
        currentDepth++;

        if (currentDepth >= limit) {
          TreeEntry treeEntry = createTreeEntry(repo, treeWalk);
          if (treeEntry != null) {
            return createFileObject(treeEntry);
          } else {
            logger.warn("could not find tree entry at {}", name);
          }
        } else {
          treeWalk.enterSubtree();
        }
      }
    }

    throw notFound(entity("File", request.getPath()).in("Revision", revId.getName()).in(this.repository));
  }

  private Map<String, SubRepository> getSubRepositories()
    throws IOException {

    logger.debug("read submodules of {} at {}", repository, revId);

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      new GitCatCommand(context, lfsBlobStoreFactory).getContent(repo, revId, PATH_MODULES, baos);
      return GitSubModuleParser.parse(baos.toString());
    } catch (NotFoundException ex) {
      logger.trace("could not find .gitmodules: {}", ex.getMessage());
      return Collections.emptyMap();
    }
  }

  @Nullable
  private SubRepository getSubRepository(String path) throws IOException {
    if (request.isDisableSubRepositoryDetection()) {
      return null;
    }

    Map<String, SubRepository> subRepositories = subrepositoryCache.get(revId);

    if (subRepositories == null) {
      subRepositories = getSubRepositories();
      subrepositoryCache.put(revId, subRepositories);
    }

    if (subRepositories != null) {
      return subRepositories.get(path);
    }
    return null;
  }

  private Optional<LfsPointer> getLfsPointer(String path, RevCommit commit, TreeEntry treeWalk) {
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
      logger.error("lfs blob for lob id {} not found in lfs store of repository {}", oid, repository);
      file.setLength(null);
    } else {
      file.setLength(blob.getSize());
    }
  }

  private class CompleteFileInformation implements Consumer<SyncAsyncExecutor.ExecutionType> {
    private final String path;
    private final FileObject file;

    public CompleteFileInformation(String path, FileObject file) {
      this.path = path;
      this.file = file;
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

    private Optional<RevCommit> getLatestCommit(org.eclipse.jgit.lib.Repository repo, ObjectId revId, String path) {
      // getLatestCommit can run async, which can lead to an open repository.
      // Because if the RepositoryService is closed and getLatestCommit runs after that it will reopen the repository.
      // So we increment the open counter and close after.
      // The increment is required to not close the repository if the getLatestCommit runs before the RepositoryService
      // is closed.
      repo.incrementOpen();
      try (RevWalk walk = new RevWalk(repo)) {
        walk.setTreeFilter(AndTreeFilter.create(TreeFilter.ANY_DIFF, PathFilter.create(path)));

        RevCommit commit = walk.parseCommit(revId);

        walk.markStart(commit);
        return of(Util.getFirst(walk));
      } catch (IOException ex) {
        logger.error("could not parse commit for file", ex);
        return empty();
      } finally {
        repo.close();
      }
    }

    private void applyValuesFromCommit(SyncAsyncExecutor.ExecutionType executionType, RevCommit commit) {
      file.setCommitDate(GitUtil.getCommitTime(commit));
      file.setDescription(commit.getShortMessage());
      if (executionType == ASYNCHRONOUS && browserResult != null) {
        updateCache();
      }
    }
  }

  private class AbortFileInformation implements Runnable {

    @Override
    public void run() {
      synchronized (asyncMonitor) {
        if (markPartialAsAborted(browserResult.getFile())) {
          updateCache();
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

  private enum TreeType {
    FILE, DIRECTORY, SUB_REPOSITORY
  }

  @Nullable
  TreeEntry createTreeEntry(org.eclipse.jgit.lib.Repository repo, TreeWalk treeWalk) throws IOException {
    String pathString = treeWalk.getPathString();
    ObjectId objectId = treeWalk.getObjectId(0);
    SubRepository subRepository = getSubRepository(pathString);
    if (subRepository != null) {
      return new TreeEntry(pathString, treeWalk.getNameString(), objectId, subRepository);
    } else if (repo.getObjectDatabase().has(objectId)) {
      TreeType type = TreeType.FILE;
      if (repo.open(objectId).getType() == Constants.OBJ_TREE) {
        type = TreeType.DIRECTORY;
      }
      return new TreeEntry(pathString, treeWalk.getNameString(), objectId, type);
    }
    return null;
  }

  private class TreeEntry {

    private final String pathString;
    private final String nameString;
    private final ObjectId objectId;
    private final TreeType type;
    private final SubRepository subRepository;
    private final List<TreeEntry> children = new ArrayList<>();

    private boolean sorted = true;

    TreeEntry() {
      pathString = "";
      nameString = "";
      objectId = null;
      subRepository = null;
      type = TreeType.DIRECTORY;
    }

    TreeEntry(String pathString, String nameString, ObjectId objectId, SubRepository subRepository) {
      this.pathString = pathString;
      this.nameString = nameString;
      this.objectId = objectId;
      this.type = TreeType.SUB_REPOSITORY;
      this.subRepository = subRepository;
    }

    TreeEntry(String pathString, String nameString, ObjectId objectId, TreeType type) {
      this.pathString = pathString;
      this.nameString = nameString;
      this.objectId = objectId;
      this.type = type;
      this.subRepository = null;
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

    SubRepository getSubRepository() {
      return subRepository;
    }

    TreeType getType() {
      return type;
    }

    List<TreeEntry> getChildren() {
      if (!sorted) {
        sort(children, entry -> entry.type != TreeType.FILE, TreeEntry::getNameString);
        sorted = true;
      }
      return children;
    }

    private void addChild(TreeEntry treeEntry) {
      sorted = false;
      children.add(treeEntry);
    }
  }

  public interface Factory {
    BrowseCommand create(GitContext context);
  }

}
