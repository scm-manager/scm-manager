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

package sonia.scm.plugin;

import com.cloudogu.scm.myevents.MyEvent;
import com.github.legman.Subscribe;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sonia.scm.EagerSingleton;
import sonia.scm.event.ScmEventBus;
import sonia.scm.xml.XmlInstantAdapter;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

@Extension(requires = "scm-landingpage-plugin")
@EagerSingleton
public class PluginInstallationFailedEventSubscriber {

  private final ScmEventBus eventBus;

  @Inject
  public PluginInstallationFailedEventSubscriber(ScmEventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Subscribe
  public void handleEvent(PluginEvent pluginEvent) {
    if (pluginEvent.getEventType() == PluginEventType.INSTALLATION_FAILED) {
      AvailablePlugin newPlugin = pluginEvent.getPlugin();

      String permission = PluginPermissions.manage().asShiroString();
      String pluginName = newPlugin.getDescriptor().getInformation().getDisplayName();

      String pluginVersion = newPlugin.getDescriptor().getInformation().getVersion();

      eventBus.post(new PluginInstallationFailedEvent(permission, pluginName, pluginVersion, Instant.now()));
    }
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  @Getter
  @NoArgsConstructor
  static class PluginInstallationFailedEvent extends MyEvent {
    private String pluginName;
    private String pluginVersion;
    @XmlJavaTypeAdapter(XmlInstantAdapter.class)
    private Instant date;

    PluginInstallationFailedEvent(String permission, String pluginName, String pluginVersion, Instant date) {
      super(PluginInstalledEventSubscriber.PluginInstalledEvent.class.getSimpleName(), permission);

      this.pluginName = pluginName;
      this.pluginVersion = pluginVersion;
      this.date = date;
    }
  }

}
