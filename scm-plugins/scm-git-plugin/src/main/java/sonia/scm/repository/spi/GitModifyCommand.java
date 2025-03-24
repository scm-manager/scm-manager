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

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.UnsupportedSigningFormatException;
import org.eclipse.jgit.attributes.AttributesNode;
import org.eclipse.jgit.attributes.AttributesRule;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.InvalidPathException;
import org.eclipse.jgit.errors.DirCacheNameConflictException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.NoChangesMadeException;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static sonia.scm.AlreadyExistsException.alreadyExists;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

public class GitModifyCommand extends AbstractGitCommand implements ModifyCommand {

  private final RepositoryManager repositoryManager;
  private final GitRepositoryHookEventFactory eventFactory;
  private final LfsBlobStoreFactory lfsBlobStoreFactory;

  private RevCommit parentCommit;

  @Inject
  GitModifyCommand(@Assisted GitContext context, LfsBlobStoreFactory lfsBlobStoreFactory, RepositoryManager repositoryManager, GitRepositoryHookEventFactory eventFactory) {
    super(context);
    this.repositoryManager = repositoryManager;
    this.eventFactory = eventFactory;
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
  }

  private interface TreeChange {
    boolean keepOriginalEntry(String path, ObjectId blob);

    default void finish(TreeHelper treeHelper) {
    }
  }

  @Override
  public String execute(ModifyCommandRequest request) {
    try {
      org.eclipse.jgit.lib.Repository repository = context.open();
      CommitHelper commitHelper = new CommitHelper(context, repositoryManager, eventFactory);
      String branchToChange = request.getBranch() == null ? context.getGlobalConfig().getDefaultBranch() : request.getBranch();
      ObjectId parentCommitId = repository.resolve(GitUtil.getRevString(branchToChange));
      if (parentCommitId == null && request.getBranch() != null && repository.resolve("HEAD") != null) {
        throw notFound(entity("Branch", branchToChange).in(this.repository));
      }
      if (request.getExpectedRevision() != null && !parentCommitId.name().equals(request.getExpectedRevision())) {
        throw new ConcurrentModificationException(entity("Branch", branchToChange).in(this.repository).build());
      }

      InPlaceWorker inPlaceWorker = new InPlaceWorker(repository);

      try (RevWalk revWalk = new RevWalk(repository)) {
        parentCommit = parentCommitId == null ? null : revWalk.parseCommit(parentCommitId);
      }

      for (ModifyCommandRequest.PartialRequest r : request.getRequests()) {
        r.execute(inPlaceWorker);
      }

      TreeHelper treeHelper = new TreeHelper(repository);
      if (parentCommitId != null) {
        treeHelper.initialize(parentCommitId, inPlaceWorker.changes);
      }

      inPlaceWorker.finish(treeHelper);

      ObjectId treeId = treeHelper.flush();

      if (parentCommitId != null) {
        if (parentCommit.getTree().equals(treeId)) {
          throw new NoChangesMadeException(GitModifyCommand.this.repository, branchToChange);
        }
      }

      ObjectId commitId = commitHelper.createCommit(
        treeId,
        request.getAuthor(),
        request.getAuthor(),
        request.getCommitMessage(),
        request.isSign(),
        parentCommitId == null ? new ObjectId[0] : new ObjectId[]{parentCommitId}
      );

      commitHelper.updateBranch(branchToChange, commitId, parentCommitId);

      return commitId.name();
    } catch (IOException | CanceledException | UnsupportedSigningFormatException e) {
      throw new InternalRepositoryException(repository, "Error during modification", e);
    }
  }

  private static String removeStartingSlash(String toBeCreated) {
    return toBeCreated.startsWith("/") ? toBeCreated.substring(1) : toBeCreated;
  }

  private class TreeHelper {

    private final org.eclipse.jgit.lib.Repository repository;
    private final DirCacheBuilder builder;
    private final ObjectInserter inserter;
    private final DirCache dirCache = DirCache.newInCore();

    TreeHelper(Repository repository) {
      this.repository = repository;
      this.inserter = repository.newObjectInserter();
      this.builder = dirCache.builder();
    }

    private void initialize(ObjectId parentCommitId, Collection<TreeChange> changes) throws IOException {
      ObjectId parentTreeId = getTreeId(parentCommitId);
      try (TreeWalk treeWalk = new TreeWalk(repository)) {

        treeWalk.addTree(parentTreeId);
        treeWalk.setRecursive(true);

        while (treeWalk.next()) {
          String path = treeWalk.getPathString();
          if (changes.stream().allMatch(c -> c.keepOriginalEntry(path, treeWalk.getObjectId(0)))) {
            DirCacheEntry entry = new DirCacheEntry(path);
            entry.setObjectId(treeWalk.getObjectId(0));
            entry.setFileMode(treeWalk.getFileMode(0));
            builder.add(entry);
          }
        }
      }
    }

    ObjectId getTreeId(ObjectId commitId) throws IOException {
      try (RevWalk revWalk = new RevWalk(repository)) {
        RevCommit commit = revWalk.parseCommit(commitId);
        return commit.getTree().getId();
      }
    }

    void updateTreeWithNewFile(String filePath, ObjectId blobId) {
      if (filePath.startsWith("/")) {
        filePath = filePath.substring(1);
      }
      try {
        DirCacheEntry newEntry = new DirCacheEntry(filePath);
        newEntry.setObjectId(blobId);
        newEntry.setFileMode(FileMode.REGULAR_FILE);
        builder.add(newEntry);
      } catch (InvalidPathException e) {
        doThrow().violation("Path", filePath).when(true);
      }
    }

    ObjectId flush() throws IOException {
      try {
        builder.finish();
      } catch (DirCacheNameConflictException e) {
        throw alreadyExists(entity("File", e.getPath1()).in(GitModifyCommand.this.repository));
      }
      ObjectId newTreeId = dirCache.writeTree(inserter);
      inserter.flush();
      return newTreeId;
    }
  }

  public interface Factory {
    ModifyCommand create(GitContext context);
  }

  private class InPlaceWorker implements Worker {
    private final Collection<TreeChange> changes = new ArrayList<>();
    private final Repository repository;
    private final Map<String, AttributesNode> attributesCache = new HashMap<>();

    public InPlaceWorker(Repository repository) {
      this.repository = repository;
    }

    @Override
    public void delete(String toBeDeleted, boolean recursive) {
      changes.add(new DeleteChange(toBeDeleted, recursive));
    }

    @Override
    public void create(String toBeCreated, File file, boolean overwrite) throws IOException {
      changes.add(new CreateChange(overwrite, toBeCreated, createBlob(toBeCreated, file)));
    }

    @Override
    public void modify(String toBeModified, File file) throws IOException {
      ObjectId blobId = createBlob(toBeModified, file);
      changes.add(new ModifyChange(toBeModified, blobId));
    }

    @Override
    public void move(String oldPath, String newPath, boolean overwrite) {
      changes.add(new MoveChange(oldPath, newPath));
    }

    public void finish(TreeHelper treeHelper) throws IOException {
      for (TreeChange c : changes) {
        c.finish(treeHelper);
      }
    }

    private ObjectId createBlob(String path, File file) throws IOException {

      try (ObjectInserter inserter = repository.newObjectInserter()) {

        if (isLfsFile(path)) {
          return writeWithLfs(file, inserter);
        } else {
          ObjectId blobId = inserter.insert(Constants.OBJ_BLOB, file.length(), new FileInputStream(file));
          inserter.flush();
          return blobId;
        }
      }
    }

    private boolean isLfsFile(String path) {
      if (parentCommit == null) {
        return false;
      }
      String[] pathParts = path.split("/");

      for (int i = pathParts.length; i > 0; --i) {
        String directory = i == 1 ? "" : String.join("/", Arrays.copyOf(pathParts, i - 1)) + "/";
        String relativeFileName = path.substring(directory.length());
        if (isLfsFile(directory, relativeFileName)) {
          return true;
        }
      }
      return false;
    }

    private boolean isLfsFile(String directory, String relativeFileName) {
      String attributesPath = directory + ".gitattributes";

      ObjectId treeId = parentCommit.getTree().getId();

      return attributesCache
        .computeIfAbsent(directory, dir -> loadAttributes(treeId, attributesPath))
        .getRules()
        .stream()
        .anyMatch(attributes -> hasLfsFilterAttribute(relativeFileName, attributes));
    }

    private boolean hasLfsFilterAttribute(String relativeFileName, AttributesRule attributes) {
      if (attributes.isMatch(relativeFileName, false)) {
        return attributes.getAttributes().stream().anyMatch(attribute -> attribute.getKey().equals("filter") && attribute.getValue().equals("lfs"));
      }
      return false;
    }

    private AttributesNode loadAttributes(ObjectId treeId, String attributesPath) {
      try (TreeWalk treeWalk = new TreeWalk(repository)) {
        treeWalk.addTree(treeId);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(attributesPath));

        AttributesNode attributesNode = new AttributesNode();
        if (treeWalk.next()) {
          ObjectId objectId = treeWalk.getObjectId(0);
          ObjectLoader loader = repository.open(objectId);
          attributesNode.parse(loader.openStream());
        }
        return attributesNode;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private ObjectId writeWithLfs(File file, ObjectInserter inserter) throws IOException {
      LfsBlobStoreCleanFilterFactory cleanFilterFactory = new LfsBlobStoreCleanFilterFactory(lfsBlobStoreFactory, GitModifyCommand.this.repository, file.toPath());
      ByteArrayOutputStream pointer = new ByteArrayOutputStream();
      cleanFilterFactory.createFilter(repository, new FileInputStream(file), pointer).run();
      ObjectId blobId = inserter.insert(Constants.OBJ_BLOB, pointer.toByteArray());
      inserter.flush();
      return blobId;
    }

    private class DeleteChange implements TreeChange {
      private final String toBeDeleted;
      private final boolean recursive;
      private final String toBeDeletedAsDirectory;
      private boolean foundOriginal;

      public DeleteChange(String toBeDeleted, boolean recursive) {
        this.toBeDeleted = removeStartingSlash(toBeDeleted);
        this.recursive = recursive;
        this.toBeDeletedAsDirectory = this.toBeDeleted + "/";
      }

      @Override
      public boolean keepOriginalEntry(String path, ObjectId blob) {
        if (path.equals(toBeDeleted) || recursive && path.startsWith(toBeDeletedAsDirectory)) {
          foundOriginal = true;
          return false;
        }
        return true;
      }

      @Override
      public void finish(TreeHelper treeHelper) {
        if (!foundOriginal) {
          throw notFound(entity("File", toBeDeleted).in(GitModifyCommand.this.repository));
        }
      }
    }

    private class CreateChange implements TreeChange {
      private final String toBeCreated;
      private final boolean overwrite;
      private final ObjectId blobId;

      public CreateChange(boolean overwrite, String toBeCreated, ObjectId blobId) {
        this.toBeCreated = removeStartingSlash(toBeCreated);
        this.overwrite = overwrite;
        this.blobId = blobId;
      }

      @Override
      public boolean keepOriginalEntry(String path, ObjectId blob) {
        if (path.equals(toBeCreated)) {
          if (!overwrite) {
            throw alreadyExists(entity("File", toBeCreated).in(GitModifyCommand.this.repository));
          }
          return false;
        }
        return true;
      }

      @Override
      public void finish(TreeHelper treeHelper) {
        treeHelper.updateTreeWithNewFile(toBeCreated, blobId);
      }
    }

    private class ModifyChange implements TreeChange {
      private final String toBeModified;
      private final ObjectId blobId;
      private boolean foundOriginal;

      public ModifyChange(String toBeModified, ObjectId blobId) {
        this.toBeModified = removeStartingSlash(toBeModified);
        this.blobId = blobId;
      }

      @Override
      public boolean keepOriginalEntry(String path, ObjectId blob) {
        if (path.equals(toBeModified)) {
          foundOriginal = true;
          return false;
        }
        return true;
      }

      @Override
      public void finish(TreeHelper treeHelper) {
        if (!foundOriginal) {
          throw notFound(entity("File", toBeModified).in(GitModifyCommand.this.repository));
        }
        treeHelper.updateTreeWithNewFile(toBeModified, blobId);
      }
    }

    private class MoveChange implements TreeChange {
      private final String oldPath;
      private final String oldPathAsDirectory;
      private final String newPath;
      private final Collection<Move> moves = new ArrayList<>();

      public MoveChange(String oldPath, String newPath) {
        this.oldPath = removeStartingSlash(oldPath);
        this.newPath = removeStartingSlash(newPath);
        this.oldPathAsDirectory = this.oldPath + "/";
      }

      @Override
      public boolean keepOriginalEntry(String path, ObjectId blob) {
        if (path.equals(oldPath) || path.startsWith(oldPathAsDirectory)) {
          moves.add(new Move(path, blob));
          return false;
        }
        return !path.equals(newPath);
      }

      @Override
      public void finish(TreeHelper treeHelper) {
        if (moves.isEmpty()) {
          throw notFound(entity("File", oldPath).in(GitModifyCommand.this.repository));
        }
        moves.forEach(move -> move.move(treeHelper));
      }

      private class Move {
        private final String to;
        private final ObjectId blobId;

        private Move(String from, ObjectId blobId) {
          this.to = from.replace(oldPath, newPath);
          this.blobId = blobId;
        }

        private void move(TreeHelper treeHelper) {
          treeHelper.updateTreeWithNewFile(to, blobId);
        }
      }
    }
  }
}
