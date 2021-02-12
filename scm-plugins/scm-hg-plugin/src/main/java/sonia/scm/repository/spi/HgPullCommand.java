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
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.event.ScmEventBus;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Person;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookTagProvider;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.PullResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
    List<sonia.scm.repository.Changeset> changesets = getChangesetFromPullResult(result);

    eventBus.post(new PostReceiveRepositoryHookEvent(createPullHookEvent(new PullHookContextProvider(tags, changesets, branches))));
  }

  private List<sonia.scm.repository.Changeset> getChangesetFromPullResult(List<Changeset> result) {
    return result.stream()
      .map(changeset -> new sonia.scm.repository.Changeset(
        changeset.toString(),
        changeset.getTimestamp().getDate().getTime(),
        Person.toPerson(changeset.getUser()),
        changeset.getMessage())
      )
      .collect(Collectors.toList());
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

  private RepositoryHookEvent createPullHookEvent(PullHookContextProvider hookEvent) {
    HookContext context = hookContextFactory.createContext(hookEvent, this.context.getScmRepository());
    return new RepositoryHookEvent(context, this.context.getScmRepository(), RepositoryHookType.POST_RECEIVE);
  }

  private static class PullHookContextProvider extends HookContextProvider {
    private final List<Tag> newTags;
    private final List<sonia.scm.repository.Changeset> newChangesets;
    private final List<String> newBranches;

    private PullHookContextProvider(List<Tag> newTags, List<sonia.scm.repository.Changeset> newChangesets, List<String> newBranches) {
      this.newTags = newTags;
      this.newChangesets = newChangesets;
      this.newBranches = newBranches;
    }

    @Override
    public Set<HookFeature> getSupportedFeatures() {
      return ImmutableSet.of(HookFeature.CHANGESET_PROVIDER, HookFeature.BRANCH_PROVIDER, HookFeature.TAG_PROVIDER);
    }

    @Override
    public HookTagProvider getTagProvider() {
      return new HookTagProvider() {
        @Override
        public List<Tag> getCreatedTags() {
          return newTags;
        }

        @Override
        public List<Tag> getDeletedTags() {
          return Collections.emptyList();
        }
      };
    }

    @Override
    public HookBranchProvider getBranchProvider() {
      return new HookBranchProvider() {
        @Override
        public List<String> getCreatedOrModified() {
          return newBranches;
        }

        @Override
        public List<String> getDeletedOrClosed() {
          return Collections.emptyList();
        }
      };
    }

    @Override
    public HookChangesetProvider getChangesetProvider() {
      return r -> new HookChangesetResponse(newChangesets);
    }
  }
}
