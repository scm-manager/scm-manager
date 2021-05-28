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
import org.eclipse.jgit.transport.TransportHttp;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.Pkcs12ClientCertificateCredential;
import sonia.scm.repository.api.UsernamePasswordCredential;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static sonia.scm.repository.GitUtil.createFetchCommandWithBranchAndTagUpdate;

public class GitMirrorCommand extends AbstractGitCommand implements MirrorCommand {

  private final PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory;
  private final MirrorHttpConnectionProvider mirrorHttpConnectionProvider;

  private final List<String> log = new ArrayList<>();

  @Inject
  GitMirrorCommand(GitContext context, PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory, MirrorHttpConnectionProvider mirrorHttpConnectionProvider) {
    super(context);
    this.postReceiveRepositoryHookEventFactory = postReceiveRepositoryHookEventFactory;
    this.mirrorHttpConnectionProvider = mirrorHttpConnectionProvider;
  }

  @Override
  public MirrorCommandResult mirror(MirrorCommandRequest mirrorCommandRequest) {
    return update(mirrorCommandRequest);
  }

  @Override
  public MirrorCommandResult update(MirrorCommandRequest mirrorCommandRequest) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    try (Repository repository = context.open(); Git git = Git.wrap(repository)) {
      return doUpdate(mirrorCommandRequest, stopwatch, repository, git);
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "error during git fetch", e);
    } catch (GitAPIException e) {
      log.add("failed to synchronize: " + e.getMessage());
      return new MirrorCommandResult(false, log, stopwatch.stop().elapsed());
    }
  }

  private MirrorCommandResult doUpdate(MirrorCommandRequest mirrorCommandRequest, Stopwatch stopwatch, Repository repository, Git git) throws GitAPIException {
    FetchResult fetchResult = createFetchCommand(mirrorCommandRequest, repository).call();
    postReceiveRepositoryHookEventFactory.fireForFetch(git, fetchResult);
    createUpdateLog(fetchResult);
    return new MirrorCommandResult(true, log, stopwatch.stop().elapsed());
  }

  private FetchCommand createFetchCommand(MirrorCommandRequest mirrorCommandRequest, Repository repository) {
    FetchCommand fetchCommand = createFetchCommandWithBranchAndTagUpdate(Git.wrap(repository))
      .setRemote(mirrorCommandRequest.getSourceUrl());

    mirrorCommandRequest.getCredential(Pkcs12ClientCertificateCredential.class)
      .ifPresent(c -> fetchCommand.setTransportConfigCallback(transport -> {
        if (transport instanceof TransportHttp) {
          TransportHttp transportHttp = (TransportHttp) transport;
          transportHttp.setHttpConnectionFactory(mirrorHttpConnectionProvider.createHttpConnectionFactory(c, log));
        }
      }));
    mirrorCommandRequest.getCredential(UsernamePasswordCredential.class)
      .ifPresent(c -> fetchCommand
        .setCredentialsProvider(
          new UsernamePasswordCredentialsProvider(
            Strings.nullToEmpty(c.username()),
            Strings.nullToEmpty(new String(c.password()))
          ))
      );

    return fetchCommand;
  }


  private void createUpdateLog(FetchResult fetchResult) {
    log.add("Branches:");
    appendRefs(fetchResult, log, "refs/heads/");
    log.add("Tags:");
    appendRefs(fetchResult, log, "refs/tags/");
  }

  private void appendRefs(FetchResult fetchResult, List<String> log, String prefix) {
    fetchResult.getTrackingRefUpdates()
      .stream()
      .filter(ref -> ref.getLocalName().startsWith(prefix))
      .forEach(
        trackingRefUpdate -> log.add(format("- %s..%s %s", trackingRefUpdate.getOldObjectId().abbreviate(9).name(), trackingRefUpdate.getNewObjectId().abbreviate(9).name(), trackingRefUpdate.getLocalName().substring(prefix.length())))
      );
  }
}
