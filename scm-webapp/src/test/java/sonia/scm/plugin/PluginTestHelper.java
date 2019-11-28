package sonia.scm.plugin;

import org.mockito.Answers;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluginTestHelper {
  public static AvailablePlugin createAvailable(String name) {
    PluginInformation information = new PluginInformation();
    information.setName(name);
    information.setVersion("1.0");
    return createAvailable(information);
  }

  public static AvailablePlugin createAvailable(String name, String version) {
    PluginInformation information = new PluginInformation();
    information.setName(name);
    information.setVersion(version);
    return createAvailable(information);
  }

  public static InstalledPlugin createInstalled(String name) {
    PluginInformation information = new PluginInformation();
    information.setName(name);
    information.setVersion("1.0");
    return createInstalled(information);
  }

  public static InstalledPlugin createInstalled(PluginInformation information) {
    InstalledPlugin plugin = mock(InstalledPlugin.class, Answers.RETURNS_DEEP_STUBS);
    returnInformation(plugin, information);
    return plugin;
  }

  public static AvailablePlugin createAvailable(PluginInformation information) {
    AvailablePluginDescriptor descriptor = mock(AvailablePluginDescriptor.class);
    lenient().when(descriptor.getInformation()).thenReturn(information);
    return new AvailablePlugin(descriptor);
  }

  private static void returnInformation(Plugin mockedPlugin, PluginInformation information) {
    when(mockedPlugin.getDescriptor().getInformation()).thenReturn(information);
  }
}
