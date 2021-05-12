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
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.UsernamePasswordCredential;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class GitMirrorCommand extends AbstractGitCommand implements MirrorCommand {

  GitMirrorCommand(GitContext context) {
    super(context);
  }

  @Override
  public MirrorCommandResult mirror(MirrorCommandRequest mirrorCommandRequest) {
    Path repositoryDir = context.getDirectory().toPath();
    if (!Files.exists(repositoryDir)) {
      createDirectories(repositoryDir);
    }

    Stopwatch stopwatch = Stopwatch.createStarted();

    CloneCommand cloneCommand = createCloneCommand(mirrorCommandRequest, repositoryDir);

    boolean success;
    String log;
    try (Git mirror = cloneCommand.call()) {
      log = createMirrorLog(mirror);
      success = true;
    } catch (GitAPIException e) {
      log = e.getMessage();
      success = false;
    }

    Duration duration = stopwatch.stop().elapsed();

    return new MirrorCommandResult(success, log, duration);
  }

  private String createMirrorLog(Git mirror) throws GitAPIException {
    StringBuilder builder = new StringBuilder();
    builder.append("Branches:\n");
    mirror.branchList().call().stream().map(Ref::getName).forEach(s -> builder.append("- ").append(s).append('\n'));
    builder.append("Tags:\n");
    mirror.tagList().call().stream().map(Ref::getName).forEach(s -> builder.append("- ").append(s).append('\n'));
    return builder.toString();
  }

  private CloneCommand createCloneCommand(MirrorCommandRequest mirrorCommandRequest, Path repositoryDir) {
    CloneCommand cloneCommand = Git.cloneRepository().setMirror(true).setGitDir(repositoryDir.toFile()).setURI(mirrorCommandRequest.getSourceUrl());
    mirrorCommandRequest.getCredentials()
      .stream()
      .filter(c -> c instanceof UsernamePasswordCredential)
      .map(c -> (UsernamePasswordCredential) c)
      .findFirst()
      .ifPresent(c -> cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
          Strings.nullToEmpty(c.username()),
          Strings.nullToEmpty(new String(c.password()))
        )
      ));
    return cloneCommand;
  }

  @Override
  public MirrorCommandResult update(MirrorCommandRequest mirrorCommandRequest) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    String log = updateMirror();
    Duration duration = stopwatch.stop().elapsed();
    return new MirrorCommandResult(true, log, duration);
  }

  private String updateMirror() {
    try (Git git = new Git(context.open())) {
      FetchResult fetchResult = git.fetch().call();
      return fetchResult.toString();
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "error during git fetch", e);
    } catch (GitAPIException e) {
      return e.getMessage();
    }
  }

  private Path createDirectories(Path repositoryDir) {
    try {
      return Files.createDirectories(repositoryDir);
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "Could not create directory for target repository", e);
    }
  }
}
