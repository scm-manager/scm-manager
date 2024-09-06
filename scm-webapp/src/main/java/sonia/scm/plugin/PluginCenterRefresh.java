/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.plugin;

import jakarta.inject.Inject;
import sonia.scm.EagerSingleton;
import sonia.scm.schedule.Scheduler;
import java.util.Random;

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
    Random random = new Random();
    scheduler.schedule(String.format("%02d %02d 0/6 * * ?", random.nextInt(60), random.nextInt(60)), RefreshTask.class);
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
