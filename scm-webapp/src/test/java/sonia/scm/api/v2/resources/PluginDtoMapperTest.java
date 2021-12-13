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

package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableSet;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.lifecycle.Restarter;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginInformation;

import java.net.URI;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static sonia.scm.plugin.PluginInformation.PluginType.*;
import static sonia.scm.plugin.PluginTestHelper.createAvailable;
import static sonia.scm.plugin.PluginTestHelper.createInstalled;

@ExtendWith(MockitoExtension.class)
class PluginDtoMapperTest {

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("https://hitchhiker.com/"));

  @Mock
  private Restarter restarter;

  @InjectMocks
  private PluginDtoMapperImpl mapper;

  @Mock
  private Subject subject;

  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldMapInformation() {
    PluginInformation information = createPluginInformation();

    PluginDto dto = new PluginDto();
    mapper.map(information, dto);

    assertThat(dto.getName()).isEqualTo("scm-cas-plugin");
    assertThat(dto.getVersion()).isEqualTo("1.0.0");
    assertThat(dto.getDisplayName()).isEqualTo("CAS");
    assertThat(dto.getAuthor()).isEqualTo("Sebastian Sdorra");
    assertThat(dto.getCategory()).isEqualTo("Authentication");
    assertThat(dto.getAvatarUrl()).isEqualTo("https://avatar.scm-manager.org/plugins/cas.png");
    assertThat(dto.getType()).isEqualTo(SCM);
  }

  private PluginInformation createPluginInformation() {
    return createPluginInformation(SCM);
  }

  private PluginInformation createPluginInformation(PluginInformation.PluginType type) {
    PluginInformation information = new PluginInformation();
    information.setName("scm-cas-plugin");
    information.setVersion("1.0.0");
    information.setDisplayName("CAS");
    information.setAuthor("Sebastian Sdorra");
    information.setCategory("Authentication");
    information.setAvatarUrl("https://avatar.scm-manager.org/plugins/cas.png");
    information.setType(type);
    return information;
  }

  @Test
  void shouldAppendInstalledSelfLink() {
    InstalledPlugin plugin = createInstalled(createPluginInformation());

    PluginDto dto = mapper.mapInstalled(plugin, emptyList());
    assertThat(dto.getLinks().getLinkBy("self").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/installed/scm-cas-plugin");
  }

  @Test
  void shouldAppendAvailableSelfLink() {
    AvailablePlugin plugin = createAvailable(createPluginInformation());

    PluginDto dto = mapper.mapAvailable(plugin);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/available/scm-cas-plugin");
  }

  @Test
  void shouldNotAppendInstallLinkWithoutPermissions() {
    AvailablePlugin plugin = createAvailable(createPluginInformation());

    PluginDto dto = mapper.mapAvailable(plugin);
    assertThat(dto.getLinks().getLinkBy("install")).isEmpty();
    assertThat(dto.getLinks().getLinkBy("installWithRestart")).isEmpty();
  }

  @Test
  void shouldAppendInstallLink() {
    when(subject.isPermitted("plugin:write")).thenReturn(true);
    AvailablePlugin plugin = createAvailable(createPluginInformation());

    PluginDto dto = mapper.mapAvailable(plugin);
    assertThat(dto.getLinks().getLinkBy("install").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/available/scm-cas-plugin/install");
  }

  @Test
  void shouldAppendCloudoguInstallLink() {
    when(subject.isPermitted("plugin:write")).thenReturn(true);
    AvailablePlugin plugin = createAvailable(createPluginInformation(CLOUDOGU));

    PluginDto dto = mapper.mapAvailable(plugin);

    assertThat(dto.getType()).isEqualTo(CLOUDOGU);
    assertThat(dto.getLinks().getLinkBy("cloudoguInstall").get().getHref())
      .isEqualTo("mycloudogu.com/install/my_plugin");
  }

  @Test
  void shouldAppendInstallWithRestartLink() {
    when(restarter.isSupported()).thenReturn(true);
    when(subject.isPermitted("plugin:write")).thenReturn(true);
    AvailablePlugin plugin = createAvailable(createPluginInformation());

    PluginDto dto = mapper.mapAvailable(plugin);
    assertThat(dto.getLinks().getLinkBy("installWithRestart").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/available/scm-cas-plugin/install?restart=true");
  }

  @Test
  void shouldNotAppendInstallLinkWithEmptyDownloadUrl() {
    when(subject.isPermitted("plugin:write")).thenReturn(true);
    AvailablePlugin plugin = createAvailable(createPluginInformation(), "");

    PluginDto dto = mapper.mapAvailable(plugin);
    assertThat(dto.getLinks().hasLink("install")).isFalse();
    assertThat(dto.getLinks().hasLink("installWithRestart")).isFalse();
  }

  @Test
  void shouldReturnMiscellaneousIfCategoryIsNull() {
    PluginInformation information = createPluginInformation();
    information.setCategory(null);
    AvailablePlugin plugin = createAvailable(information);
    PluginDto dto = mapper.mapAvailable(plugin);
    assertThat(dto.getCategory()).isEqualTo("Miscellaneous");
  }

  @Test
  void shouldAppendDependencies() {
    AvailablePlugin plugin = createAvailable(createPluginInformation());
    when(plugin.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("one", "two"));

    PluginDto dto = mapper.mapAvailable(plugin);
    assertThat(dto.getDependencies()).containsOnly("one", "two");
  }

  @Test
  void shouldAppendOptionalDependencies() {
    AvailablePlugin plugin = createAvailable(createPluginInformation());
    when(plugin.getDescriptor().getOptionalDependencies()).thenReturn(ImmutableSet.of("one", "two"));

    PluginDto dto = mapper.mapAvailable(plugin);
    assertThat(dto.getOptionalDependencies()).containsOnly("one", "two");
  }

  @Test
  void shouldAppendUninstallLink() {
    when(subject.isPermitted("plugin:write")).thenReturn(true);
    InstalledPlugin plugin = createInstalled(createPluginInformation());
    when(plugin.isUninstallable()).thenReturn(true);

    PluginDto dto = mapper.mapInstalled(plugin, emptyList());
    assertThat(dto.getLinks().getLinkBy("uninstall").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/installed/scm-cas-plugin/uninstall");
  }

  @Test
  void shouldAppendUninstallWithRestartLink() {
    when(restarter.isSupported()).thenReturn(true);
    when(subject.isPermitted("plugin:write")).thenReturn(true);

    InstalledPlugin plugin = createInstalled(createPluginInformation());
    when(plugin.isUninstallable()).thenReturn(true);

    PluginDto dto = mapper.mapInstalled(plugin, emptyList());
    assertThat(dto.getLinks().getLinkBy("uninstallWithRestart").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/installed/scm-cas-plugin/uninstall?restart=true");
  }
}
