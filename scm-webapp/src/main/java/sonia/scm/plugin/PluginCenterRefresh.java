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

import sonia.scm.EagerSingleton;
import sonia.scm.schedule.Scheduler;

import javax.inject.Inject;

/**
 * Refresh plugin center cache and refresh the token of plugin center authentication.
 * @since 2.28.0
 */
@Extension
@EagerSingleton
public class PluginCenterRefresh {

  @Inject
  @SuppressWarnings("java:S1118") // could not hide constructor
  public PluginCenterRefresh(Scheduler scheduler) {
    scheduler.schedule("42 42 0/6 * * ?", RefreshTask.class);
  }

  public static class RefreshTask implements Runnable {

    private final PluginCenter pluginCenter;

    @Inject
    public RefreshTask(PluginCenter pluginCenter) {
      this.pluginCenter = pluginCenter;
    }

    @Override
    public void run() {
      pluginCenter.refresh();
    }
  }

}
