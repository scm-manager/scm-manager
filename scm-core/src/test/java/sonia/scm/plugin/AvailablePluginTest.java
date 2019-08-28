package sonia.scm.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AvailablePluginTest {

  @Mock
  private AvailablePluginDescriptor descriptor;

  @Test
  void shouldReturnNewPendingPluginOnInstall() {
    AvailablePlugin plugin = new AvailablePlugin(descriptor);
    assertThat(plugin.isPending()).isFalse();

    AvailablePlugin installed = plugin.install();
    assertThat(installed.isPending()).isTrue();
  }

  @Test
  void shouldThrowIllegalStateExceptionIfAlreadyPending() {
    AvailablePlugin plugin = new AvailablePlugin(descriptor).install();
    assertThrows(IllegalStateException.class, () -> plugin.install());
  }

}
