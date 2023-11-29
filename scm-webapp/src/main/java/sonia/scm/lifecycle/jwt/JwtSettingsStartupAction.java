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
