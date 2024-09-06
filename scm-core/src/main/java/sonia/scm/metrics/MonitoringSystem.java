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

package sonia.scm.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import sonia.scm.plugin.ExtensionPoint;

import java.util.Optional;

/**
 * Extension point to pass SCM-Manager metrics to a monitoring system.
 *
 * @since 2.15.0
 */
@ExtensionPoint
public interface MonitoringSystem {

  String getName();

  /**
   * Returns registry of metrics provider.
   */
  MeterRegistry getRegistry();

  /**
   * Returns an optional scrape target.
   * A scrape target is only needed if the monitoring system pulls the metrics over http.
   * If the monitoring system uses a push based model, this method returns an empty optional.
   *
   * @return optional scrape target
   */
  default Optional<ScrapeTarget> getScrapeTarget() {
    return Optional.empty();
  }
}
