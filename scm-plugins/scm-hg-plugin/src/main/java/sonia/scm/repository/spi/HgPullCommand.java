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

import com.aragost.javahg.Changeset;
import com.aragost.javahg.commands.ExecutionException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.event.ScmEventBus;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.PullResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class HgPullCommand extends AbstractHgPushOrPullCommand implements PullCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgPullCommand.class);
  private static final String AUTH_SECTION = "auth";
  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;

  public HgPullCommand(HgRepositoryHandler handler, HgCommandContext context, HookContextFactory hookContextFactory, ScmEventBus eventBus) {
    super(handler, context);
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
  }

  @Override
  @SuppressWarnings({"java:S3252"})
  public PullResponse pull(PullCommandRequest request)
    throws IOException {
    String url = getRemoteUrl(request);

    LOG.debug("pull changes from {} to {}", url, getContext().getScmRepository());

    List<Changeset> result;

    if (!Strings.isNullOrEmpty(request.getUsername()) && !Strings.isNullOrEmpty(request.getPassword())) {
      addAuthenticationConfig(request, url);
    }

    try {
      result = com.aragost.javahg.commands.PullCommand.on(open()).execute(url);
    } catch (ExecutionException ex) {
      throw new ImportFailedException(ContextEntry.ContextBuilder.entity(getRepository()).build(), "could not execute pull command", ex);
    } finally {
      removeAuthenticationConfig();
    }

    firePostReceiveRepositoryHookEvent(result);

    return new PullResponse(result.size());
  }

  private void firePostReceiveRepositoryHookEvent(List<Changeset> result) {
    List<String> branches = getBranchesFromPullResult(result);
    List<Tag> tags = getTagsFromPullResult(result);

    eventBus.post(createEvent(branches, tags, new HgLazyChangesetResolver(context.open())));
  }

  private List<Tag> getTagsFromPullResult(List<Changeset> result) {
    return result.stream()
      .flatMap(changeset -> changeset.tags().stream())
      .collect(Collectors.toList())
      .stream()
      .map(tag -> new Tag(tag, tag))
      .distinct()
      .collect(Collectors.toList());
  }

  private List<String> getBranchesFromPullResult(List<Changeset> result) {
    return result.stream()
      .map(Changeset::getBranch)
      .distinct()
      .collect(Collectors.toList());
  }

  private PostReceiveRepositoryHookEvent createEvent(List<String> branches, List<Tag> tags, HgLazyChangesetResolver changesetResolver) {
    HookContext context = hookContextFactory.createContext(new HgImportHookContextProvider(branches, tags, changesetResolver), this.context.getScmRepository());
    RepositoryHookEvent repositoryHookEvent = new RepositoryHookEvent(context, this.context.getScmRepository(), RepositoryHookType.POST_RECEIVE);
    return new PostReceiveRepositoryHookEvent(repositoryHookEvent);
  }

  public void addAuthenticationConfig(PullCommandRequest request, String url) throws IOException {
    INIConfiguration ini = readIniConfiguration();
    INISection authSection = ini.getSection(AUTH_SECTION);
    if (authSection == null) {
      authSection = new INISection(AUTH_SECTION);
      ini.addSection(authSection);
    }
    URI parsedUrl = URI.create(url);
    authSection.setParameter("import.prefix", parsedUrl.getHost());
    authSection.setParameter("import.schemes", parsedUrl.getScheme());
    authSection.setParameter("import.username", request.getUsername());
    authSection.setParameter("import.password", request.getPassword());
    writeIniConfiguration(ini);
  }

  public void removeAuthenticationConfig() throws IOException {
    INIConfiguration ini = readIniConfiguration();
    ini.removeSection(AUTH_SECTION);
    writeIniConfiguration(ini);
  }

  public INIConfiguration readIniConfiguration() throws IOException {
    return new INIConfigurationReader().read(getHgrcFile());
  }

  public void writeIniConfiguration(INIConfiguration ini) throws IOException {
    new INIConfigurationWriter().write(ini, getHgrcFile());
  }

  public File getHgrcFile() {
    return new File(getContext().getDirectory(), HgRepositoryHandler.PATH_HGRC);
  }
}
