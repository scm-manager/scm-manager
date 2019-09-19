package sonia.scm.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.event.ScmEventBus;
import sonia.scm.lifecycle.RestartEvent;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.plugin.PluginTestHelper.createAvailable;
import static sonia.scm.plugin.PluginTestHelper.createInstalled;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class DefaultPluginManagerTest {

  @Mock
  private ScmEventBus eventBus;

  @Mock
  private PluginLoader loader;

  @Mock
  private PluginCenter center;

  @Mock
  private PluginInstaller installer;

  @InjectMocks
  private DefaultPluginManager manager;

  @Mock
  private Subject subject;

  @BeforeEach
  void mockInstaller() {
    lenient().when(installer.install(any())).then(ic -> {
      AvailablePlugin plugin = ic.getArgument(0);
      return new PendingPluginInstallation(plugin.install(), null);
    });
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

      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, git));

      List<AvailablePlugin> available = manager.getAvailable();
      assertThat(available).containsOnly(review, git);
    }

    @Test
    void shouldFilterOutAllInstalled() {
      InstalledPlugin installedGit = createInstalled("scm-git-plugin");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(installedGit));

      AvailablePlugin review = createAvailable("scm-review-plugin");
      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, git));

      List<AvailablePlugin> available = manager.getAvailable();
      assertThat(available).containsOnly(review);
    }

    @Test
    void shouldReturnAvailable() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, git));

      Optional<AvailablePlugin> available = manager.getAvailable("scm-git-plugin");
      assertThat(available).contains(git);
    }

    @Test
    void shouldReturnEmptyForNonExistingAvailable() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review));

      Optional<AvailablePlugin> available = manager.getAvailable("scm-git-plugin");
      assertThat(available).isEmpty();
    }

    @Test
    void shouldReturnEmptyForInstalledPlugin() {
      InstalledPlugin installedGit = createInstalled("scm-git-plugin");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(installedGit));

      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(git));

      Optional<AvailablePlugin> available = manager.getAvailable("scm-git-plugin");
      assertThat(available).isEmpty();
    }

    @Test
    void shouldInstallThePlugin() {
      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(git));

      manager.install("scm-git-plugin", false);

      verify(installer).install(git);
      verify(eventBus, never()).post(any());
    }

    @Test
    void shouldInstallDependingPlugins() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, mail));

      manager.install("scm-review-plugin", false);

      verify(installer).install(mail);
      verify(installer).install(review);
    }

    @Test
    void shouldNotInstallAlreadyInstalledDependencies() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, mail));

      InstalledPlugin installedMail = createInstalled("scm-mail-plugin");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(installedMail));

      manager.install("scm-review-plugin", false);

      ArgumentCaptor<AvailablePlugin> captor = ArgumentCaptor.forClass(AvailablePlugin.class);
      verify(installer).install(captor.capture());

      assertThat(captor.getValue().getDescriptor().getInformation().getName()).isEqualTo("scm-review-plugin");
    }

    @Test
    void shouldRollbackOnFailedInstallation() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin");
      when(mail.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-notification-plugin"));
      AvailablePlugin notification = createAvailable("scm-notification-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, mail, notification));

      PendingPluginInstallation pendingNotification = mock(PendingPluginInstallation.class);
      doReturn(pendingNotification).when(installer).install(notification);

      PendingPluginInstallation pendingMail = mock(PendingPluginInstallation.class);
      doReturn(pendingMail).when(installer).install(mail);

      doThrow(new PluginChecksumMismatchException("checksum does not match")).when(installer).install(review);

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
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, mail));

      assertThrows(NotFoundException.class, () -> manager.install("scm-review-plugin", false));

      verify(installer, never()).install(any());
    }

    @Test
    void shouldSendRestartEventAfterInstallation() {
      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(git));

      manager.install("scm-git-plugin", true);

      verify(installer).install(git);
      verify(eventBus).post(any(RestartEvent.class));
    }

    @Test
    void shouldNotSendRestartEventIfNoPluginWasInstalled() {
      InstalledPlugin gitInstalled = createInstalled("scm-git-plugin");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(gitInstalled));

      manager.install("scm-git-plugin", true);
      verify(eventBus, never()).post(any());
    }

    @Test
    void shouldNotInstallAlreadyPendingPlugins() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review));

      manager.install("scm-review-plugin", false);
      manager.install("scm-review-plugin", false);
      // only one interaction
      verify(installer).install(any());
    }

    @Test
    void shouldSendRestartEvent() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review));

      manager.install("scm-review-plugin", false);
      manager.executePendingAndRestart();

      verify(eventBus).post(any(RestartEvent.class));
    }

    @Test
    void shouldNotSendRestartEventWithoutPendingPlugins() {
      manager.executePendingAndRestart();

      verify(eventBus, never()).post(any());
    }

    @Test
    void shouldReturnSingleAvailableAsPending() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review));

      manager.install("scm-review-plugin", false);

      Optional<AvailablePlugin> available = manager.getAvailable("scm-review-plugin");
      assertThat(available.get().isPending()).isTrue();
    }

    @Test
    void shouldReturnAvailableAsPending() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review));

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
    void shouldCreateUninstallFile(@TempDirectory.TempDir Path temp) {
      InstalledPlugin mailPlugin = createInstalled("scm-mail-plugin");
      when(mailPlugin.getDirectory()).thenReturn(temp);

      when(loader.getInstalledPlugins()).thenReturn(singletonList(mailPlugin));

      manager.uninstall("scm-mail-plugin", false);

      assertThat(temp.resolve("uninstall")).exists();
    }

    @Test
    void shouldMarkPluginForUninstall(@TempDirectory.TempDir Path temp) {
      InstalledPlugin mailPlugin = createInstalled("scm-mail-plugin");
      when(mailPlugin.getDirectory()).thenReturn(temp);

      when(loader.getInstalledPlugins()).thenReturn(singletonList(mailPlugin));

      manager.uninstall("scm-mail-plugin", false);

      verify(mailPlugin).setMarkedForUninstall(true);
    }

    @Test
    void shouldThrowExceptionWhenUninstallingCorePlugin(@TempDirectory.TempDir Path temp) {
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
      when(center.getAvailable()).thenReturn(singleton(reviewPlugin));

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

      verify(eventBus).post(any(RestartEvent.class));
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
    }

  }

  @Nested
  class WithoutManagePermissions {

    @BeforeEach
    void setUpSubject() {
      ThreadContext.bind(subject);
      doThrow(AuthorizationException.class).when(subject).checkPermission("plugin:manage");
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
    void shouldThrowAuthorizationExceptionsForUninstallMethod() {
      assertThrows(AuthorizationException.class, () -> manager.uninstall("test", false));
    }

    @Test
    void shouldThrowAuthorizationExceptionsForExecutePendingAndRestart() {
      assertThrows(AuthorizationException.class, () -> manager.executePendingAndRestart());
    }

  }
}
