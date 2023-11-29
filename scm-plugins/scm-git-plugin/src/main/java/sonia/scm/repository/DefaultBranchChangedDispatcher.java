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

package sonia.scm.repository;

import com.github.legman.Subscribe;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import sonia.scm.EagerSingleton;
import sonia.scm.api.v2.resources.GitRepositoryConfigChangedEvent;
import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;

@Extension
@EagerSingleton
public class DefaultBranchChangedDispatcher {

  private final ScmEventBus eventBus;

  @Inject
  public DefaultBranchChangedDispatcher(ScmEventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Subscribe
  public void handleConfigUpdate(GitRepositoryConfigChangedEvent event) {
    String oldDefaultBranch = event.getOldConfig().getDefaultBranch();
    String newDefaultBranch = event.getNewConfig().getDefaultBranch();
    if (hasChanged(oldDefaultBranch, newDefaultBranch)) {
      eventBus.post(new DefaultBranchChangedEvent(event.getRepository(), oldDefaultBranch, newDefaultBranch));
    }
  }

  private boolean hasChanged(String oldDefaultBranch, String newDefaultBranch) {
    return !Strings.nullToEmpty(oldDefaultBranch).equals(Strings.nullToEmpty(newDefaultBranch));
  }
}
