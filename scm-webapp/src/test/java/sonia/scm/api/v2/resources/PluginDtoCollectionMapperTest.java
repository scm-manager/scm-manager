package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.AvailablePluginDescriptor;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.InstalledPluginDescriptor;
import sonia.scm.plugin.PluginInformation;

import java.net.URI;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginDtoCollectionMapperTest {

  ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("/"));

  @InjectMocks
  PluginDtoMapperImpl pluginDtoMapper;

  Subject subject = mock(Subject.class);
  ThreadState subjectThreadState = new SubjectThreadState(subject);

  @BeforeEach
  void bindSubject() {
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @AfterEach
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }


  @Test
  void shouldMapInstalledPluginsWithoutUpdateWhenNoNewerVersionIsAvailable() {
    PluginDtoCollectionMapper mapper = new PluginDtoCollectionMapper(resourceLinks, pluginDtoMapper);

    HalRepresentation result = mapper.mapInstalled(
      singletonList(createInstalledPlugin("scm-some-plugin", "1")),
      singletonList(createAvailablePlugin("scm-other-plugin", "2")));

    List<HalRepresentation> plugins = result.getEmbedded().getItemsBy("plugins");
    assertThat(plugins).hasSize(1);
    PluginDto plugin = (PluginDto) plugins.get(0);
    assertThat(plugin.getVersion()).isEqualTo("1");
    assertThat(plugin.getNewVersion()).isNull();
  }

  @Test
  void shouldSetNewVersionInInstalledPluginWhenAvailableVersionIsNewer() {
    PluginDtoCollectionMapper mapper = new PluginDtoCollectionMapper(resourceLinks, pluginDtoMapper);

    HalRepresentation result = mapper.mapInstalled(
      singletonList(createInstalledPlugin("scm-some-plugin", "1")),
      singletonList(createAvailablePlugin("scm-some-plugin", "2")));

    PluginDto plugin = getPluginDtoFromResult(result);
    assertThat(plugin.getVersion()).isEqualTo("1");
    assertThat(plugin.getNewVersion()).isEqualTo("2");
  }

  @Test
  void shouldNotAddInstallLinkForNewVersionWhenNotPermitted() {
    when(subject.isPermitted("plugin:manage")).thenReturn(false);
    PluginDtoCollectionMapper mapper = new PluginDtoCollectionMapper(resourceLinks, pluginDtoMapper);

    HalRepresentation result = mapper.mapInstalled(
      singletonList(createInstalledPlugin("scm-some-plugin", "1")),
      singletonList(createAvailablePlugin("scm-some-plugin", "2")));

    PluginDto plugin = getPluginDtoFromResult(result);
    assertThat(plugin.getLinks().getLinkBy("update")).isEmpty();
  }

  @Test
  void shouldNotAddInstallLinkForNewVersionWhenInstallationIsPending() {
    when(subject.isPermitted("plugin:manage")).thenReturn(true);
    PluginDtoCollectionMapper mapper = new PluginDtoCollectionMapper(resourceLinks, pluginDtoMapper);

    AvailablePlugin availablePlugin = createAvailablePlugin("scm-some-plugin", "2");
    when(availablePlugin.isPending()).thenReturn(true);
    HalRepresentation result = mapper.mapInstalled(
      singletonList(createInstalledPlugin("scm-some-plugin", "1")),
      singletonList(availablePlugin));

    PluginDto plugin = getPluginDtoFromResult(result);
    assertThat(plugin.getLinks().getLinkBy("update")).isEmpty();
  }

  @Test
  void shouldAddInstallLinkForNewVersionWhenPermitted() {
    when(subject.isPermitted("plugin:manage")).thenReturn(true);
    PluginDtoCollectionMapper mapper = new PluginDtoCollectionMapper(resourceLinks, pluginDtoMapper);

    HalRepresentation result = mapper.mapInstalled(
      singletonList(createInstalledPlugin("scm-some-plugin", "1")),
      singletonList(createAvailablePlugin("scm-some-plugin", "2")));

    PluginDto plugin = getPluginDtoFromResult(result);
    assertThat(plugin.getLinks().getLinkBy("update")).isNotEmpty();
  }

  @Test
  void shouldSetInstalledPluginPendingWhenCorrespondingAvailablePluginIsPending() {
    when(subject.isPermitted("plugin:manage")).thenReturn(true);
    PluginDtoCollectionMapper mapper = new PluginDtoCollectionMapper(resourceLinks, pluginDtoMapper);

    AvailablePlugin availablePlugin = createAvailablePlugin("scm-some-plugin", "2");
    when(availablePlugin.isPending()).thenReturn(true);
    HalRepresentation result = mapper.mapInstalled(
      singletonList(createInstalledPlugin("scm-some-plugin", "1")),
      singletonList(availablePlugin));

    PluginDto plugin = getPluginDtoFromResult(result);
    assertThat(plugin.isPending()).isTrue();
  }

  private PluginDto getPluginDtoFromResult(HalRepresentation result) {
    assertThat(result.getEmbedded().getItemsBy("plugins")).hasSize(1);
    List<HalRepresentation> plugins = result.getEmbedded().getItemsBy("plugins");
    return (PluginDto) plugins.get(0);
  }

  private InstalledPlugin createInstalledPlugin(String name, String version) {
    PluginInformation information = new PluginInformation();
    information.setName(name);
    information.setVersion(version);
    return createInstalledPlugin(information);
  }

  private InstalledPlugin createInstalledPlugin(PluginInformation information) {
    InstalledPlugin plugin = mock(InstalledPlugin.class);
    InstalledPluginDescriptor descriptor = mock(InstalledPluginDescriptor.class);
    lenient().when(descriptor.getInformation()).thenReturn(information);
    lenient().when(plugin.getDescriptor()).thenReturn(descriptor);
    return plugin;
  }

  private AvailablePlugin createAvailablePlugin(String name, String version) {
    PluginInformation information = new PluginInformation();
    information.setName(name);
    information.setVersion(version);
    return createAvailablePlugin(information);
  }

  private AvailablePlugin createAvailablePlugin(PluginInformation information) {
    AvailablePlugin plugin = mock(AvailablePlugin.class);
    AvailablePluginDescriptor descriptor = mock(AvailablePluginDescriptor.class);
    lenient().when(descriptor.getInformation()).thenReturn(information);
    lenient().when(plugin.getDescriptor()).thenReturn(descriptor);
    return plugin;
  }
}
