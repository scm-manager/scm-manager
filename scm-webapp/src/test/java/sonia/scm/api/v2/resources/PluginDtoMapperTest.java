package sonia.scm.api.v2.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginState;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PluginDtoMapperTest {

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("https://hitchhiker.com/"));

  @InjectMocks
  private PluginDtoMapperImpl mapper;

  @Test
  void shouldMapInformation() {
    PluginInformation information = createPluginInformation();

    PluginDto dto = mapper.map(information);

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
    PluginInformation information = createPluginInformation();
    information.setState(PluginState.INSTALLED);

    PluginDto dto = mapper.map(information);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/installed/scm-cas-plugin");
  }

  @Test
  void shouldAppendAvailableSelfLink() {
    PluginInformation information = createPluginInformation();
    information.setState(PluginState.AVAILABLE);

    PluginDto dto = mapper.map(information);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/available/scm-cas-plugin/1.0.0");
  }

  @Test
  void shouldAppendInstallLink() {
    PluginInformation information = createPluginInformation();
    information.setState(PluginState.AVAILABLE);

    PluginDto dto = mapper.map(information);
    assertThat(dto.getLinks().getLinkBy("install").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/available/scm-cas-plugin/1.0.0/install");
  }

  @Test
  void shouldReturnMiscellaneousIfCategoryIsNull() {
    PluginInformation information = createPluginInformation();
    information.setCategory(null);

    PluginDto dto = mapper.map(information);
    assertThat(dto.getCategory()).isEqualTo("Miscellaneous");
  }

}
