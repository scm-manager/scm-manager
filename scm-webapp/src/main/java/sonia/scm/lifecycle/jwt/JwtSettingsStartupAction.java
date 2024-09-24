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

package sonia.scm.lifecycle.jwt;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.lifecycle.PrivilegedStartupAction;
import sonia.scm.plugin.Extension;
import sonia.scm.security.JwtConfig;

import java.time.Clock;
import java.time.Instant;

@Extension
public class JwtSettingsStartupAction implements PrivilegedStartupAction {

  private static final Logger LOG = LoggerFactory.getLogger(JwtSettingsStartupAction.class);
  private final JwtSettingsStore store;
  private final JwtConfig jwtConfig;
  private final Clock clock;

  @Inject
  public JwtSettingsStartupAction(JwtSettingsStore store, JwtConfig jwtConfig) {
    this(store, jwtConfig, Clock.systemDefaultZone());
  }

  public JwtSettingsStartupAction(JwtSettingsStore store, JwtConfig jwtConfig, Clock clock) {
    this.store = store;
    this.jwtConfig = jwtConfig;
    this.clock = clock;
  }

  @Override
  public void run() {
    LOG.debug("Checking JWT Settings");

    JwtSettings settings = store.get();
    boolean isEndlessJwtEnabledNow = jwtConfig.isEndlessJwtEnabled();

    if(!areSettingsChanged(settings, isEndlessJwtEnabledNow)) {
      LOG.debug("JWT Settings unchanged");
      return;
    }

    JwtSettings updatedSettings = new JwtSettings();
    updatedSettings.setEndlessJwtEnabledLastStartUp(isEndlessJwtEnabledNow);

    if(areEndlessJwtNeedingInvalidation(settings, isEndlessJwtEnabledNow)) {
      updatedSettings.setKeysValidAfterTimestampInMs(Instant.now(clock).toEpochMilli());
    } else {
      updatedSettings.setKeysValidAfterTimestampInMs(settings.getKeysValidAfterTimestampInMs());
    }

    store.set(updatedSettings);

    LOG.debug("JWT Settings updated");
  }

  private boolean areSettingsChanged(JwtSettings settings, boolean isEndlessJwtEnabledNow) {
    return settings.isEndlessJwtEnabledLastStartUp() != isEndlessJwtEnabledNow;
  }

  private boolean areEndlessJwtNeedingInvalidation(JwtSettings settings, boolean isEndlessJwtEnabledNow) {
    return settings.isEndlessJwtEnabledLastStartUp() && !isEndlessJwtEnabledNow;
  }
}
