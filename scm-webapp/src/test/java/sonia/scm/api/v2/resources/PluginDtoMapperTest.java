package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginDescriptor;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginState;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginDtoMapperTest {

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("https://hitchhiker.com/"));

  @InjectMocks
  private PluginDtoMapperImpl mapper;

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
  }

  private PluginInformation createPluginInformation() {
    PluginInformation information = new PluginInformation();
    information.setName("scm-cas-plugin");
    information.setVersion("1.0.0");
    information.setDisplayName("CAS");
    information.setAuthor("Sebastian Sdorra");
    information.setCategory("Authentication");
    information.setAvatarUrl("https://avatar.scm-manager.org/plugins/cas.png");
    return information;
  }

  @Test
  void shouldAppendInstalledSelfLink() {
    Plugin plugin = createPlugin(PluginState.INSTALLED);

    PluginDto dto = mapper.map(plugin);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/installed/scm-cas-plugin");
  }

  @Test
  void shouldAppendAvailableSelfLink() {
    Plugin plugin = createPlugin(PluginState.AVAILABLE);

    PluginDto dto = mapper.map(plugin);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/available/scm-cas-plugin");
  }

  @Test
  void shouldAppendInstallLink() {
    Plugin plugin = createPlugin(PluginState.AVAILABLE);

    PluginDto dto = mapper.map(plugin);
    assertThat(dto.getLinks().getLinkBy("install").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/available/scm-cas-plugin/install");
  }

  @Test
  void shouldReturnMiscellaneousIfCategoryIsNull() {
    PluginInformation information = createPluginInformation();
    information.setCategory(null);
    Plugin plugin = createPlugin(information, PluginState.AVAILABLE);
    PluginDto dto = mapper.map(plugin);
    assertThat(dto.getCategory()).isEqualTo("Miscellaneous");
  }

  @Test
  void shouldAppendDependencies() {
    Plugin plugin = createPlugin(PluginState.AVAILABLE);
    when(plugin.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("one", "two"));

    PluginDto dto = mapper.map(plugin);
    assertThat(dto.getDependencies()).containsOnly("one", "two");
  }

  private Plugin createPlugin(PluginState state) {
    return createPlugin(createPluginInformation(), state);
  }

  private Plugin createPlugin(PluginInformation information, PluginState state) {
    Plugin plugin = Mockito.mock(Plugin.class);
    when(plugin.getState()).thenReturn(state);
    PluginDescriptor descriptor = mock(PluginDescriptor.class);
    when(descriptor.getInformation()).thenReturn(information);
    when(plugin.getDescriptor()).thenReturn(descriptor);
    return plugin;
  }

}
