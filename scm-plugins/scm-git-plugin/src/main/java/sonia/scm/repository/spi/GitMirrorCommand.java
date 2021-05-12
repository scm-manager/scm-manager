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
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.UsernamePasswordCredential;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;

public class GitMirrorCommand extends AbstractGitCommand implements MirrorCommand {

  private final PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory;

  @Inject
  GitMirrorCommand(GitContext context, PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory) {
    super(context);
    this.postReceiveRepositoryHookEventFactory = postReceiveRepositoryHookEventFactory;
  }

  @Override
  public MirrorCommandResult mirror(MirrorCommandRequest mirrorCommandRequest) {
    return update(mirrorCommandRequest);
  }

  private FetchCommand createFetchCommand(MirrorCommandRequest mirrorCommandRequest, Repository repository) {
    FetchCommand fetchCommand = Git.wrap(repository)
      .fetch()
      .setRefSpecs(new RefSpec("refs/heads/*:refs/heads/*"))
      .setTagOpt(TagOpt.FETCH_TAGS)
      .setRemote(mirrorCommandRequest.getSourceUrl());
    mirrorCommandRequest.getCredentials()
      .stream()
      .filter(c -> c instanceof UsernamePasswordCredential)
      .map(c -> (UsernamePasswordCredential) c)
      .findFirst()
      .ifPresent(c -> fetchCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
          Strings.nullToEmpty(c.username()),
          Strings.nullToEmpty(new String(c.password()))
        )
      ));

    return fetchCommand;
  }

  @Override
  public MirrorCommandResult update(MirrorCommandRequest mirrorCommandRequest) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    boolean success;
    String log;
    try (Repository repository = context.open(); Git git = Git.wrap(repository)) {
      FetchResult fetchResult = createFetchCommand(mirrorCommandRequest, repository).call();
      postReceiveRepositoryHookEventFactory.fireForFetch(git, fetchResult);
      log = createUpdateLog(fetchResult);
      success = true;
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "error during git fetch", e);
    } catch (GitAPIException e) {
      log = e.getMessage();
      success = false;
    }
    Duration duration = stopwatch.stop().elapsed();
    return new MirrorCommandResult(success, log, duration);
  }

  private String createUpdateLog(FetchResult fetchResult) {
    StringBuilder builder = new StringBuilder();
    builder.append("Branches:\n");
    appendRefs(fetchResult, builder, "refs/heads/");
    builder.append("Tags:\n");
    appendRefs(fetchResult, builder, "refs/tags/");
    return builder.toString();
  }

  private void appendRefs(FetchResult fetchResult, StringBuilder builder, String prefix) {
    fetchResult.getTrackingRefUpdates()
      .stream()
      .filter(ref -> ref.getLocalName().startsWith(prefix))
      .forEach(
        trackingRefUpdate -> builder.append("- ").append(trackingRefUpdate.getOldObjectId().abbreviate(9).name()).append("...").append(trackingRefUpdate.getNewObjectId().abbreviate(9).name()).append(' ').append(trackingRefUpdate.getLocalName().substring(prefix.length())).append('\n')
      );
  }
}
