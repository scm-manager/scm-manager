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

package sonia.scm.config;

import sonia.scm.event.Event;

/**
 * The {@link ScmConfigurationChangedEvent} is fired whenever the
 * {@link ScmConfiguration} changes.
 *
 * @since 1.34
 */
@Event
public final class ScmConfigurationChangedEvent {

  private final ScmConfiguration configuration;

  public ScmConfigurationChangedEvent(ScmConfiguration configuration) {
    this.configuration = configuration;
  }

  public ScmConfiguration getConfiguration() {
    return configuration;
  }
}
