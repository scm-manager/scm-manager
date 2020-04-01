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

import com.cloudogu.scm.myevents.MyEvent;
import com.github.legman.Subscribe;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shiro.SecurityUtils;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;
import sonia.scm.user.User;
import sonia.scm.xml.XmlInstantAdapter;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

@Extension(requires = "scm-landingpage-plugin")
@EagerSingleton
public class RepositoryCreatedEventSubscriber {

  private final ScmEventBus eventBus;

  @Inject
  public RepositoryCreatedEventSubscriber(ScmEventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Subscribe
  public void handleEvent(RepositoryEvent event) {
    if (event.getEventType() == HandlerEventType.CREATE) {
      Repository eventRepo = event.getItem();
      String permission = RepositoryPermissions.read(event.getItem()).asShiroString();
      String repository = eventRepo.getNamespace() + "/" + eventRepo.getName();
      String creator = SecurityUtils.getSubject().getPrincipals().oneByType(User.class).getDisplayName();
      Instant date = Instant.now();

      eventBus.post(new RepositoryCreatedEvent(permission, repository, creator, date));
    }
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  @Getter
  @NoArgsConstructor
  static class RepositoryCreatedEvent extends MyEvent {
    private String repository;
    private String creator;
    @XmlJavaTypeAdapter(XmlInstantAdapter.class)
    private Instant date;

    RepositoryCreatedEvent(String permission, String repository, String creator, Instant date) {
      super(RepositoryCreatedEvent.class.getSimpleName(), permission);
      this.repository = repository;
      this.creator = creator;
      this.date = date;
    }
  }

}
