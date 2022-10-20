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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.event.ScmEventBus;
import sonia.scm.lifecycle.Restarter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.plugin.PluginTestHelper.createAvailable;
import static sonia.scm.plugin.PluginTestHelper.createInstalled;

@ExtendWith(MockitoExtension.class)
class DefaultPluginManagerTest {

  @Mock
  private PluginLoader loader;

  @Mock
  private PluginCenter center;

  @Mock
  private PluginInstaller installer;

  @Mock
  private Restarter restarter;

  @Mock
  private PluginSetConfigStore pluginSetConfigStore;

  @Mock
  private ScmEventBus eventBus;

  @Captor
  private ArgumentCaptor<PluginEvent> eventCaptor;

  @Captor
  private ArgumentCaptor<PluginInstallationContext> contextCaptor;

  private DefaultPluginManager manager;

  @Mock
  private Subject subject;

  private final PluginInstallationContext context = PluginInstallationContext.empty();

  @BeforeEach
  void mockInstaller() {
    lenient().when(installer.install(any(), any())).then(ic -> {
      AvailablePlugin plugin = ic.getArgument(1);
      return new PendingPluginInstallation(plugin.install(), null);
    });
  }

  @BeforeEach
  void setUpObjectUnderTest() {
    manager = new DefaultPluginManager(
      loader, center, installer, restarter, eventBus, plugins -> context, pluginSetConfigStore
    );
  }

  @Nested
  class WithAdminPermissions {

    @BeforeEach
    void setUpSubject() {
      ThreadContext.bind(subject);
    }

    @AfterEach
    void clearThreadContext() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldReturnInstalledPlugins() {
      InstalledPlugin review = createInstalled("scm-review-plugin");
      InstalledPlugin git = createInstalled("scm-git-plugin");

      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(review, git));

      List<InstalledPlugin> installed = manager.getInstalled();
      assertThat(installed).containsOnly(review, git);
    }

    @Test
    void shouldReturnReviewPlugin() {
      InstalledPlugin review = createInstalled("scm-review-plugin");
      InstalledPlugin git = createInstalled("scm-git-plugin");

      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(review, git));

      Optional<InstalledPlugin> plugin = manager.getInstalled("scm-review-plugin");
      assertThat(plugin).contains(review);
    }

    @Test
    void shouldReturnEmptyForNonInstalledPlugin() {
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of());

      Optional<InstalledPlugin> plugin = manager.getInstalled("scm-review-plugin");
      assertThat(plugin).isEmpty();
    }

    @Test
    void shouldReturnAvailablePlugins() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      AvailablePlugin git = createAvailable("scm-git-plugin");

      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review, git));

      List<AvailablePlugin> available = manager.getAvailable();
      assertThat(available).containsOnly(review, git);
    }

    @Test
    void shouldFilterOutAllInstalled() {
      InstalledPlugin installedGit = createInstalled("scm-git-plugin");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(installedGit));

      AvailablePlugin review = createAvailable("scm-review-plugin");
      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review, git));

      List<AvailablePlugin> available = manager.getAvailable();
      assertThat(available).containsOnly(review);
    }

    @Test
    void shouldReturnAvailable() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review, git));

      Optional<AvailablePlugin> available = manager.getAvailable("scm-git-plugin");
      assertThat(available).contains(git);
    }

    @Test
    void shouldReturnEmptyForNonExistingAvailable() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review));

      Optional<AvailablePlugin> available = manager.getAvailable("scm-git-plugin");
      assertThat(available).isEmpty();
    }

    @Test
    void shouldReturnEmptyForInstalledPlugin() {
      InstalledPlugin installedGit = createInstalled("scm-git-plugin");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(installedGit));

      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(git));

      Optional<AvailablePlugin> available = manager.getAvailable("scm-git-plugin");
      assertThat(available).isEmpty();
    }

    @Test
    void shouldInstallThePlugin() {
      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(git));

      manager.install("scm-git-plugin", false);

      verify(installer).install(context, git);
      verify(restarter, never()).restart(any(), any());
    }

    @Test
    void shouldInstallDependingPlugins() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review, mail));

      manager.install("scm-review-plugin", false);

      verify(installer).install(context, mail);
      verify(installer).install(context, review);
    }

    @Test
    void shouldInstallMultipleSameDependingPluginsOnlyOnce() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      AvailablePlugin ci = createAvailable("scm-ci-plugin", "1.1.0");
      InstalledPlugin oldCi = createInstalled("scm-ci-plugin", "1.0.0");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin", "scm-ci-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin");
      when(mail.getDescriptor().getOptionalDependencies()).thenReturn(ImmutableSet.of("scm-ci-plugin"));
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review, mail, ci));
      when(loader.getInstalledPlugins()).thenReturn(ImmutableSet.of(oldCi));

      manager.install("scm-review-plugin", false);

      verify(installer).install(context, mail);
      verify(installer).install(context, review);
      verify(installer, times(1)).install(context, ci);
    }

    @Test
    void shouldNotInstallAlreadyInstalledDependenciesWhenUpToDate() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review, mail));

      InstalledPlugin installedMail = createInstalled("scm-mail-plugin");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(installedMail));

      manager.install("scm-review-plugin", false);

      ArgumentCaptor<AvailablePlugin> captor = ArgumentCaptor.forClass(AvailablePlugin.class);
      verify(installer).install(any(), captor.capture());

      assertThat(captor.getValue().getDescriptor().getInformation().getName()).isEqualTo("scm-review-plugin");
    }

    @Test
    void shouldUpdateAlreadyInstalledDependenciesWhenNewerVersionIsAvailable() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin", "1.1.0");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review, mail));

      InstalledPlugin installedMail = createInstalled("scm-mail-plugin", "1.0.0");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(installedMail));

      manager.install("scm-review-plugin", false);

      verify(installer).install(context, mail);
      verify(installer).install(context, review);
    }

    @Test
    void shouldUpdateAlreadyInstalledOptionalDependenciesWhenNewerVersionIsAvailable() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getOptionalDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin", "1.1.0");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review, mail));

      InstalledPlugin installedMail = createInstalled("scm-mail-plugin", "1.0.0");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(installedMail));

      manager.install("scm-review-plugin", false);

      verify(installer).install(context, mail);
      verify(installer).install(context, review);
    }

    @Test
    void shouldNotUpdateOptionalDependenciesWhenNewerVersionIsAvailableButItIsNotInstalled() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getOptionalDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin", "1.1.0");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review, mail));

      manager.install("scm-review-plugin", false);

      verify(installer, never()).install(context, mail);
      verify(installer).install(context, review);
    }

    @Test
    void shouldRollbackOnFailedInstallation() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin");
      when(mail.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-notification-plugin"));
      AvailablePlugin notification = createAvailable("scm-notification-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review, mail, notification));

      PendingPluginInstallation pendingNotification = mock(PendingPluginInstallation.class);
      doReturn(pendingNotification).when(installer).install(context, notification);

      PendingPluginInstallation pendingMail = mock(PendingPluginInstallation.class);
      doReturn(pendingMail).when(installer).install(context, mail);

      doThrow(new PluginChecksumMismatchException(mail, "1", "2")).when(installer).install(context, review);

      assertThrows(PluginInstallException.class, () -> manager.install("scm-review-plugin", false));

      verify(pendingNotification).cancel();
      verify(pendingMail).cancel();
    }

    @Test
    void shouldInstallNothingIfOneOfTheDependenciesIsNotAvailable() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin");
      when(mail.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-notification-plugin"));
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review, mail));

      assertThrows(NotFoundException.class, () -> manager.install("scm-review-plugin", false));

      verify(installer, never()).install(any(), any());
    }

    @Test
    void shouldSendRestartEventAfterInstallation() {
      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(git));

      manager.install("scm-git-plugin", true);

      verify(installer).install(context, git);
      verify(restarter).restart(any(), any());
    }

    @Test
    void shouldNotSendRestartEventIfNoPluginWasInstalled() {
      InstalledPlugin gitInstalled = createInstalled("scm-git-plugin");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(gitInstalled));

      manager.install("scm-git-plugin", true);
      verify(restarter, never()).restart(any(), any());
    }

    @Test
    void shouldNotInstallAlreadyPendingPlugins() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review));

      manager.install("scm-review-plugin", false);
      manager.install("scm-review-plugin", false);
      // only one interaction
      verify(installer).install(any(), any());
    }

    @Test
    void shouldSendRestartEvent() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review));

      manager.install("scm-review-plugin", false);
      manager.executePendingAndRestart();

      verify(restarter).restart(any(), any());
    }

    @Test
    void shouldNotSendRestartEventWithoutPendingPlugins() {
      manager.executePendingAndRestart();

      verify(restarter, never()).restart(any(), any());
    }

    @Test
    void shouldReturnSingleAvailableAsPending() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review));

      manager.install("scm-review-plugin", false);

      Optional<AvailablePlugin> available = manager.getAvailable("scm-review-plugin");
      assertThat(available.get().isPending()).isTrue();
    }

    @Test
    void shouldReturnAvailableAsPending() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review));

      manager.install("scm-review-plugin", false);

      List<AvailablePlugin> available = manager.getAvailable();
      assertThat(available.get(0).isPending()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenUninstallingUnknownPlugin() {
      assertThrows(NotFoundException.class, () -> manager.uninstall("no-such-plugin", false));
    }

    @Test
    void shouldUseDependencyTrackerForUninstall() {
      InstalledPlugin mailPlugin = createInstalled("scm-mail-plugin");
      InstalledPlugin reviewPlugin = createInstalled("scm-review-plugin");
      when(reviewPlugin.getDescriptor().getDependencies()).thenReturn(singleton("scm-mail-plugin"));

      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(mailPlugin, reviewPlugin));
      manager.computeInstallationDependencies();

      assertThrows(ScmConstraintViolationException.class, () -> manager.uninstall("scm-mail-plugin", false));
    }

    @Test
    void shouldCreateUninstallFile(@TempDir Path temp) {
      InstalledPlugin mailPlugin = createInstalled("scm-mail-plugin");
      when(mailPlugin.getDirectory()).thenReturn(temp);

      when(loader.getInstalledPlugins()).thenReturn(singletonList(mailPlugin));

      manager.uninstall("scm-mail-plugin", false);

      assertThat(temp.resolve("uninstall")).exists();
    }

    @Test
    void shouldMarkPluginForUninstall(@TempDir Path temp) {
      InstalledPlugin mailPlugin = createInstalled("scm-mail-plugin");
      when(mailPlugin.getDirectory()).thenReturn(temp);

      when(loader.getInstalledPlugins()).thenReturn(singletonList(mailPlugin));

      manager.uninstall("scm-mail-plugin", false);

      verify(mailPlugin).setMarkedForUninstall(true);
    }

    @Test
    void shouldNotChangeStateWhenUninstallFileCouldNotBeCreated() {
      InstalledPlugin mailPlugin = createInstalled("scm-mail-plugin");
      InstalledPlugin reviewPlugin = createInstalled("scm-review-plugin");
      when(reviewPlugin.getDescriptor().getDependencies()).thenReturn(singleton("scm-mail-plugin"));

      when(reviewPlugin.getDirectory()).thenThrow(new PluginException("when the file could not be written an exception like this is thrown"));

      when(loader.getInstalledPlugins()).thenReturn(asList(mailPlugin, reviewPlugin));

      manager.computeInstallationDependencies();

      assertThrows(PluginException.class, () -> manager.uninstall("scm-review-plugin", false));

      verify(mailPlugin, never()).setMarkedForUninstall(true);
      assertThrows(ScmConstraintViolationException.class, () -> manager.uninstall("scm-mail-plugin", false));
    }

    @Test
    void shouldThrowExceptionWhenUninstallingCorePlugin(@TempDir Path temp) {
      InstalledPlugin mailPlugin = createInstalled("scm-mail-plugin");
      when(mailPlugin.getDirectory()).thenReturn(temp);
      when(mailPlugin.isCore()).thenReturn(true);

      when(loader.getInstalledPlugins()).thenReturn(singletonList(mailPlugin));

      assertThrows(ScmConstraintViolationException.class, () -> manager.uninstall("scm-mail-plugin", false));

      assertThat(temp.resolve("uninstall")).doesNotExist();
    }

    @Test
    void shouldMarkUninstallablePlugins() {
      InstalledPlugin mailPlugin = createInstalled("scm-mail-plugin");
      InstalledPlugin reviewPlugin = createInstalled("scm-review-plugin");
      when(reviewPlugin.getDescriptor().getDependencies()).thenReturn(singleton("scm-mail-plugin"));

      when(loader.getInstalledPlugins()).thenReturn(asList(mailPlugin, reviewPlugin));

      manager.computeInstallationDependencies();

      verify(reviewPlugin).setUninstallable(true);
      verify(mailPlugin).setUninstallable(false);
    }

    @Test
    void shouldUpdateMayUninstallFlagAfterDependencyIsUninstalled() {
      InstalledPlugin mailPlugin = createInstalled("scm-mail-plugin");
      InstalledPlugin reviewPlugin = createInstalled("scm-review-plugin");
      when(reviewPlugin.getDescriptor().getDependencies()).thenReturn(singleton("scm-mail-plugin"));

      when(loader.getInstalledPlugins()).thenReturn(asList(mailPlugin, reviewPlugin));

      manager.computeInstallationDependencies();

      manager.uninstall("scm-review-plugin", false);

      verify(mailPlugin).setUninstallable(true);
    }

    @Test
    void shouldUpdateMayUninstallFlagAfterDependencyIsInstalled() {
      InstalledPlugin mailPlugin = createInstalled("scm-mail-plugin");
      AvailablePlugin reviewPlugin = createAvailable("scm-review-plugin");
      when(reviewPlugin.getDescriptor().getDependencies()).thenReturn(singleton("scm-mail-plugin"));

      when(loader.getInstalledPlugins()).thenReturn(singletonList(mailPlugin));
      when(center.getAvailablePlugins()).thenReturn(singleton(reviewPlugin));

      manager.computeInstallationDependencies();

      manager.install("scm-review-plugin", false);

      verify(mailPlugin).setUninstallable(false);
    }

    @Test
    void shouldRestartWithUninstallOnly() {
      InstalledPlugin mailPlugin = createInstalled("scm-mail-plugin");
      when(mailPlugin.isMarkedForUninstall()).thenReturn(true);

      when(loader.getInstalledPlugins()).thenReturn(singletonList(mailPlugin));

      manager.executePendingAndRestart();

      verify(restarter).restart(any(), any());
    }

    @Test
    void shouldUndoPendingInstallations(@TempDir Path temp) throws IOException {
      InstalledPlugin mailPlugin = createInstalled("scm-ssh-plugin");
      Path mailPluginPath = temp.resolve("scm-mail-plugin");
      Files.createDirectories(mailPluginPath);
      when(mailPlugin.getDirectory()).thenReturn(mailPluginPath);
      when(loader.getInstalledPlugins()).thenReturn(singletonList(mailPlugin));
      ArgumentCaptor<Boolean> uninstallCaptor = ArgumentCaptor.forClass(Boolean.class);
      doNothing().when(mailPlugin).setMarkedForUninstall(uninstallCaptor.capture());

      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(git));
      PendingPluginInstallation gitPendingPluginInformation = mock(PendingPluginInstallation.class);
      when(installer.install(context, git)).thenReturn(gitPendingPluginInformation);

      manager.install("scm-git-plugin", false);
      manager.uninstall("scm-ssh-plugin", false);

      manager.cancelPending();

      assertThat(mailPluginPath.resolve("uninstall")).doesNotExist();
      verify(gitPendingPluginInformation).cancel();
      Boolean lasUninstallMarkerSet = uninstallCaptor.getAllValues().get(uninstallCaptor.getAllValues().size() - 1);
      assertThat(lasUninstallMarkerSet).isFalse();

      Files.createFile(mailPluginPath.resolve("uninstall"));

      manager.cancelPending();
      verify(gitPendingPluginInformation, times(1)).cancel();
      assertThat(mailPluginPath.resolve("uninstall")).exists();
    }

    @Test
    void shouldUpdateAllPlugins() {
      InstalledPlugin mailPlugin = createInstalled("scm-mail-plugin");
      InstalledPlugin reviewPlugin = createInstalled("scm-review-plugin");

      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(mailPlugin, reviewPlugin));

      AvailablePlugin newMailPlugin = createAvailable("scm-mail-plugin", "2.0.0");
      AvailablePlugin newReviewPlugin = createAvailable("scm-review-plugin", "2.0.0");

      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(newMailPlugin, newReviewPlugin));

      manager.updateAll();

      verify(installer).install(context, newMailPlugin);
      verify(installer).install(context, newReviewPlugin);
    }


    @Test
    void shouldNotUpdateToOldPluginVersions() {
      InstalledPlugin scriptPlugin = createInstalled("scm-script-plugin");

      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(scriptPlugin));
      AvailablePlugin oldScriptPlugin = createAvailable("scm-script-plugin", "0.9");

      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(oldScriptPlugin));

      manager.updateAll();

      verify(installer, never()).install(context, oldScriptPlugin);
    }

    @Test
    void shouldFirePluginEventOnInstallation() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review));

      manager.install("scm-review-plugin", false);

      verify(eventBus).post(eventCaptor.capture());

      assertThat(eventCaptor.getValue().getEventType()).isEqualTo(PluginEvent.PluginEventType.INSTALLED);
      assertThat(eventCaptor.getValue().getPlugin()).isEqualTo(review);
    }

    @Test
    void shouldFirePluginEventOnFailedInstallation() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(review));
      doThrow(new PluginDownloadException(review, new IOException())).when(installer).install(context, review);

      assertThrows(PluginDownloadException.class, () -> manager.install("scm-review-plugin", false));

      verify(eventBus).post(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getEventType()).isEqualTo(PluginEvent.PluginEventType.INSTALLATION_FAILED);
      assertThat(eventCaptor.getValue().getPlugin()).isEqualTo(review);
    }

    @Test
    void contextShouldContainAvailablePluginsAndPendingInstallationPlugins() {
      DefaultPluginManager manager = new DefaultPluginManager(
        loader, center, installer, restarter, eventBus, null
      );

      AvailablePlugin jenkins = createAvailable("scm-jenkins-plugin");
      AvailablePlugin webhook = createAvailable("scm-webhook-plugin");
      when(jenkins.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-el-plugin"));
      when(webhook.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-el-plugin"));
      AvailablePlugin el = createAvailable("scm-el-plugin");
      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(jenkins, el, webhook));

      manager.install("scm-jenkins-plugin", false);
      manager.install("scm-webhook-plugin", false);

      verify(installer, times(3)).install(contextCaptor.capture(), any());

      PluginInstallationContext pluginInstallationContext = contextCaptor.getAllValues().get(2);

      assertThat(pluginInstallationContext.find("scm-jenkins-plugin")).isPresent();
      assertThat(pluginInstallationContext.find("scm-webhook-plugin")).isPresent();
      assertThat(pluginInstallationContext.find("scm-el-plugin")).isPresent();
    }

    @Test
    void shouldGetPluginSets() {
      PluginSet pluginSet = new PluginSet(
        "my-plugin-set",
        0,
        ImmutableSet.of("scm-jenkins-plugin", "scm-webhook-plugin", "scm-el-plugin"),
        ImmutableMap.of("en", new PluginSet.Description("My Plugin Set", List.of("this is awesome!"))),
        ImmutableMap.of("standard", "base64image")
      );
      when(center.getAvailablePluginSets()).thenReturn(ImmutableSet.of(pluginSet));
      Set<PluginSet> pluginSets = manager.getPluginSets();
      assertThat(pluginSets).containsExactly(pluginSet);
    }

    @Test
    void shouldInstallPluginSets() {
      AvailablePlugin git = createAvailable("scm-git-plugin");
      AvailablePlugin svn = createAvailable("scm-svn-plugin");
      AvailablePlugin hg = createAvailable("scm-hg-plugin");

      when(center.getAvailablePlugins()).thenReturn(ImmutableSet.of(git, svn, hg));

      PluginSet pluginSet = new PluginSet(
        "my-plugin-set",
        0,
        ImmutableSet.of("scm-git-plugin", "scm-hg-plugin"),
        ImmutableMap.of("en", new PluginSet.Description("My Plugin Set", List.of("this is awesome!"))),
        ImmutableMap.of("standard", "base64image")
      );

      PluginSet pluginSet2 = new PluginSet(
        "my-other-plugin-set",
        0,
        ImmutableSet.of("scm-svn-plugin", "scm-hg-plugin"),
        ImmutableMap.of("en", new PluginSet.Description("My Plugin Set", List.of("this is awesome!"))),
        ImmutableMap.of("standard", "base64image")
      );
      when(center.getAvailablePluginSets()).thenReturn(ImmutableSet.of(pluginSet, pluginSet2));

      manager.installPluginSets(ImmutableSet.of("my-plugin-set", "my-other-plugin-set"), false);

      verify(pluginSetConfigStore).setPluginSets(new PluginSetsConfig(ImmutableSet.of("my-plugin-set", "my-other-plugin-set")));
      verify(installer, Mockito.times(1)).install(context, git);
      verify(installer, Mockito.times(1)).install(context, hg);
      verify(installer, Mockito.times(1)).install(context, svn);

      verify(restarter, never()).restart(any(), any());
    }
  }

  @Nested
  class WithoutReadPermissions {

    @BeforeEach
    void setUpSubject() {
      ThreadContext.bind(subject);
      doThrow(AuthorizationException.class).when(subject).checkPermission("plugin:read");
    }

    @AfterEach
    void clearThreadContext() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldThrowAuthorizationExceptionsForReadMethods() {
      assertThrows(AuthorizationException.class, () -> manager.getInstalled());
      assertThrows(AuthorizationException.class, () -> manager.getInstalled("test"));
      assertThrows(AuthorizationException.class, () -> manager.getAvailable());
      assertThrows(AuthorizationException.class, () -> manager.getAvailable("test"));
      assertThrows(AuthorizationException.class, () -> manager.getPluginSets());
    }

  }

  @Nested
  class WithoutWritePermissions {

    @BeforeEach
    void setUpSubject() {
      ThreadContext.bind(subject);
      doThrow(AuthorizationException.class).when(subject).checkPermission("plugin:write");
    }

    @AfterEach
    void clearThreadContext() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldThrowAuthorizationExceptionsForInstallMethod() {
      assertThrows(AuthorizationException.class, () -> manager.install("test", false));
    }

    @Test
    void shouldThrowAuthorizationExceptionsForInstallPluginSetsMethod() {
      ImmutableSet<String> pluginSetIds = ImmutableSet.of("test");
      assertThrows(AuthorizationException.class, () -> manager.installPluginSets(pluginSetIds, false));
    }

    @Test
    void shouldThrowAuthorizationExceptionsForUninstallMethod() {
      assertThrows(AuthorizationException.class, () -> manager.uninstall("test", false));
    }

    @Test
    void shouldThrowAuthorizationExceptionsForExecutePendingAndRestart() {
      assertThrows(AuthorizationException.class, () -> manager.executePendingAndRestart());
    }

    @Test
    void shouldThrowAuthorizationExceptionsForCancelPending() {
      assertThrows(AuthorizationException.class, () -> manager.cancelPending());
    }

    @Test
    void shouldThrowAuthorizationExceptionsForUpdateAll() {
      assertThrows(AuthorizationException.class, () -> manager.updateAll());
    }
  }
}
