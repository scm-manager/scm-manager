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
    
package sonia.scm.user;

import com.github.legman.Subscribe;
import jakarta.inject.Inject;
import sonia.scm.ContextEntry;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AnonymousMode;

@EagerSingleton
@Extension
public class AnonymousUserDeletionEventHandler {

  private final ScmConfiguration scmConfiguration;

  @Inject
  public AnonymousUserDeletionEventHandler(ScmConfiguration scmConfiguration) {
    this.scmConfiguration = scmConfiguration;
  }

  @Subscribe(async = false)
  public void onEvent(UserEvent event) {
    if (isAnonymousUserDeletionNotAllowed(event)) {
      throw new AnonymousUserDeletionException(ContextEntry.ContextBuilder.entity(User.class, event.getItem().getId()));
    }
  }

  private boolean isAnonymousUserDeletionNotAllowed(UserEvent event) {
    return event.getEventType() == HandlerEventType.BEFORE_DELETE
      && event.getItem().getName().equals(SCMContext.USER_ANONYMOUS)
      && scmConfiguration.getAnonymousMode() != AnonymousMode.OFF;
  }
}
