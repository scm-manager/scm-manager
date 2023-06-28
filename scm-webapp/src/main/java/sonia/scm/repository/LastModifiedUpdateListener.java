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
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.NotFoundException;
import sonia.scm.plugin.Extension;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

/**
 * @author Sebastian Sdorra
 * @since 1.37
 */
@Extension
@EagerSingleton
public final class LastModifiedUpdateListener {

  private static final Logger LOG = LoggerFactory.getLogger(LastModifiedUpdateListener.class);

  private final AdministrationContext adminContext;

  private final RepositoryManager repositoryManager;

  @Inject
  public LastModifiedUpdateListener(AdministrationContext adminContext,
                                    RepositoryManager repositoryManager) {
    this.adminContext = adminContext;
    this.repositoryManager = repositoryManager;
  }

  @Subscribe
  public void onPostReceive(PostReceiveRepositoryHookEvent event) {
    final Repository repository = event.getRepository();

    if (repository != null) {
      //J-
      adminContext.runAsAdmin(
        new LastModifiedPrivilegedAction(repositoryManager, repository)
      );
      //J+
    } else {
      LOG.warn("received hook without repository");
    }
  }

  static class LastModifiedPrivilegedAction implements PrivilegedAction {

    private final Repository repository;

    private final RepositoryManager repositoryManager;

    public LastModifiedPrivilegedAction(RepositoryManager repositoryManager,
                                        Repository repository) {
      this.repositoryManager = repositoryManager;
      this.repository = repository;
    }

    @Override
    public void run() {
      Repository dbr = repositoryManager.get(repository.getId());

      if (dbr != null) {
        LOG.debug("update last modified date of repository {}", dbr.getId());
        dbr.setLastModified(System.currentTimeMillis());

        try {
          repositoryManager.modify(dbr);
        } catch (NotFoundException e) {
          LOG.error("could not modify repository", e);
        }
      } else {
        LOG.error("could not find repository with id {}", repository.getId());
      }
    }
  }
}
