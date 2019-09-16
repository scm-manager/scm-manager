/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

import java.util.List;
import java.util.Optional;

/**
 * The plugin manager is responsible for plugin related tasks, such as install, uninstall or updating.
 *
 * @author Sebastian Sdorra
 */
public interface PluginManager {

  /**
   * Returns the available plugin with the given name.
   * @param name of plugin
   * @return optional available plugin.
   */
  Optional<AvailablePlugin> getAvailable(String name);

  /**
   * Returns the installed plugin with the given name.
   * @param name of plugin
   * @return optional installed plugin.
   */
  Optional<InstalledPlugin> getInstalled(String name);


  /**
   * Returns all installed plugins.
   *
   * @return a list of installed plugins.
   */
  List<InstalledPlugin> getInstalled();

  /**
   * Returns all available plugins. The list contains the plugins which are loaded from the plugin center, but without
   * the installed plugins.
   *
   * @return a list of available plugins.
   */
  List<AvailablePlugin> getAvailable();

  /**
   * Installs the plugin with the given name from the list of available plugins.
   *
   * @param name plugin name
   * @param restartAfterInstallation restart context after plugin installation
   */
  void install(String name, boolean restartAfterInstallation);

  /**
   * Marks the plugin with the given name for uninstall.
   *
   * @param name plugin name
   * @param restartAfterInstallation restart context after plugin has been marked to really uninstall the plugin
   */
  void uninstall(String name, boolean restartAfterInstallation);

  /**
   * Install all pending plugins and restart the scm context.
   */
  void installPendingAndRestart();
}
