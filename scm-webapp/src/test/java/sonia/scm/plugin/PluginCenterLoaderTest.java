package sonia.scm.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpClient;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginCenterLoaderTest {

  private static final String PLUGIN_URL = "https://plugins.hitchhiker.com";

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private AdvancedHttpClient client;

  @Mock
  private PluginCenterDtoMapper mapper;

  @InjectMocks
  private PluginCenterLoader loader;

  @Test
  void shouldFetch() throws IOException {
    Set<AvailablePlugin> plugins = Collections.emptySet();
    PluginCenterDto dto = new PluginCenterDto();
    when(client.get(PLUGIN_URL).request().contentFromJson(PluginCenterDto.class)).thenReturn(dto);
    when(mapper.map(dto)).thenReturn(plugins);

    Set<AvailablePlugin> fetched = loader.load(PLUGIN_URL);
    assertThat(fetched).isSameAs(plugins);
  }

  @Test
  void shouldReturnEmptySetIfPluginCenterNotBeReached() throws IOException {
    when(client.get(PLUGIN_URL).request()).thenThrow(new IOException("failed to fetch"));

    Set<AvailablePlugin> fetch = loader.load(PLUGIN_URL);
    assertThat(fetch).isEmpty();
  }
}
