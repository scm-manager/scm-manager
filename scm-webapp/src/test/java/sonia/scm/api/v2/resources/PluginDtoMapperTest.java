package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableSet;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.AvailablePluginDescriptor;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginInformation;

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
    InstalledPlugin plugin = createInstalled();

    PluginDto dto = mapper.mapInstalled(plugin);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/installed/scm-cas-plugin");
  }

  private InstalledPlugin createInstalled(PluginInformation information) {
    InstalledPlugin plugin = mock(InstalledPlugin.class, Answers.RETURNS_DEEP_STUBS);
    when(plugin.getDescriptor().getInformation()).thenReturn(information);
    return plugin;
  }

  @Test
  void shouldAppendAvailableSelfLink() {
    AvailablePlugin plugin = createAvailable();

    PluginDto dto = mapper.mapAvailable(plugin);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/available/scm-cas-plugin");
  }

  @Test
  void shouldNotAppendInstallLinkWithoutPermissions() {
    AvailablePlugin plugin = createAvailable();

    PluginDto dto = mapper.mapAvailable(plugin);
    assertThat(dto.getLinks().getLinkBy("install")).isEmpty();
  }

  @Test
  void shouldAppendInstallLink() {
    when(subject.isPermitted("plugin:manage")).thenReturn(true);
    AvailablePlugin plugin = createAvailable();

    PluginDto dto = mapper.mapAvailable(plugin);
    assertThat(dto.getLinks().getLinkBy("install").get().getHref())
      .isEqualTo("https://hitchhiker.com/v2/plugins/available/scm-cas-plugin/install");
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
    AvailablePlugin plugin = createAvailable();
    when(plugin.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("one", "two"));

    PluginDto dto = mapper.mapAvailable(plugin);
    assertThat(dto.getDependencies()).containsOnly("one", "two");
  }

  private InstalledPlugin createInstalled() {
    return createInstalled(createPluginInformation());
  }

  private AvailablePlugin createAvailable() {
    return createAvailable(createPluginInformation());
  }

  private AvailablePlugin createAvailable(PluginInformation information) {
    AvailablePluginDescriptor descriptor = mock(AvailablePluginDescriptor.class);
    when(descriptor.getInformation()).thenReturn(information);
    return new AvailablePlugin(descriptor);
  }

}
