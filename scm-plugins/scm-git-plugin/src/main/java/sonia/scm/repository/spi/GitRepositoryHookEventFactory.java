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

import jakarta.inject.Inject;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;

import java.io.IOException;
import java.util.List;

import static sonia.scm.repository.RepositoryHookType.POST_RECEIVE;

class GitRepositoryHookEventFactory {

  private final HookContextFactory hookContextFactory;
  private final GitChangesetConverterFactory changesetConverterFactory;

  @Inject
  public GitRepositoryHookEventFactory(HookContextFactory hookContextFactory, GitChangesetConverterFactory changesetConverterFactory) {
    this.hookContextFactory = hookContextFactory;
    this.changesetConverterFactory = changesetConverterFactory;
  }

  RepositoryHookEvent createEvent(GitContext gitContext,
                                             List<String> branches,
                                             List<Tag> tags,
                                             GitLazyChangesetResolver changesetResolver
  ) throws IOException {
    GitChangesetConverter converter = changesetConverterFactory.create(gitContext.open());
    GitImportHookContextProvider contextProvider = new GitImportHookContextProvider(converter, branches, tags, changesetResolver);
    HookContext context = hookContextFactory.createContext(contextProvider, gitContext.getRepository());
    return new RepositoryHookEvent(context, gitContext.getRepository(), POST_RECEIVE);
  }
}
