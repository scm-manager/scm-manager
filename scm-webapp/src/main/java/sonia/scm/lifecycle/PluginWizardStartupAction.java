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

package sonia.scm.lifecycle;

import sonia.scm.initialization.InitializationStep;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.PluginSetConfigStore;

import javax.inject.Inject;
import javax.inject.Singleton;

@Extension
@Singleton
public class PluginWizardStartupAction implements InitializationStep {

  private final PluginSetConfigStore store;

  @Inject
  public PluginWizardStartupAction(PluginSetConfigStore pluginSetConfigStore) {
    this.store = pluginSetConfigStore;
  }

  @Override
  public String name() {
    return "pluginWizard";
  }

  @Override
  public int sequence() {
    return 1;
  }

  @Override
  public boolean done() {
    return System.getProperty(AdminAccountStartupAction.INITIAL_PASSWORD_PROPERTY) != null || store.getPluginSets().isPresent();
  }

}
