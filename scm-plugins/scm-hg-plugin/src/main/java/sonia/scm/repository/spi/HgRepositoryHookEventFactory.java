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
import jakarta.inject.Inject;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;

import java.util.List;

import static sonia.scm.repository.RepositoryHookType.POST_RECEIVE;
import static sonia.scm.repository.spi.HgBranchesTagsExtractor.extractBranches;
import static sonia.scm.repository.spi.HgBranchesTagsExtractor.extractTags;

public class HgRepositoryHookEventFactory {

  private final HookContextFactory hookContextFactory;

  @Inject
  public HgRepositoryHookEventFactory(HookContextFactory hookContextFactory) {
    this.hookContextFactory = hookContextFactory;
  }

  RepositoryHookEvent createEvent(@Assisted HgCommandContext hgContext, HgLazyChangesetResolver changesetResolver) {
    List<String> branches = extractBranches(hgContext);
    List<Tag> tags = extractTags(hgContext);
    HgImportHookContextProvider contextProvider = new HgImportHookContextProvider(branches, tags, changesetResolver);
    HookContext context = hookContextFactory.createContext(contextProvider, hgContext.getScmRepository());
    return new RepositoryHookEvent(context, hgContext.getScmRepository(), POST_RECEIVE);
  }

  public interface Factory {
    HgRepositoryHookEventFactory create(HgCommandContext context);
  }

}
