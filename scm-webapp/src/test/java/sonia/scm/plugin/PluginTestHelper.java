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
