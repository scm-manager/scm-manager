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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class PendingPluginInstallation {

  private static final Logger LOG = LoggerFactory.getLogger(PendingPluginInstallation.class);

  private final AvailablePlugin plugin;
  private final Path file;

  PendingPluginInstallation(AvailablePlugin plugin, Path file) {
    this.plugin = plugin;
    this.file = file;
  }

  public AvailablePlugin getPlugin() {
    return plugin;
  }

  void cancel() {
    String name = plugin.getDescriptor().getInformation().getName();
    LOG.info("cancel installation of plugin {}", name);
    try {
      Files.delete(file);
    } catch (IOException ex) {
      throw new PluginFailedToCancelInstallationException("failed to cancel plugin installation ", name, ex);
    }
  }
}
