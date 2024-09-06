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

package sonia.scm.update.plugin;

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.PluginCenterAuthenticator;
import sonia.scm.security.CipherUtil;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.version.Version;

import static sonia.scm.version.Version.parse;

@Extension
public class PluginCenterAuthenticationUpdateStep implements UpdateStep {

  private final ConfigurationStoreFactory configurationStoreFactory;

  @Inject
  public PluginCenterAuthenticationUpdateStep(ConfigurationStoreFactory configurationStoreFactory) {
    this.configurationStoreFactory = configurationStoreFactory;
  }

  @Override
  public void doUpdate() throws Exception {
    ConfigurationStore<PluginCenterAuthenticator.Authentication> configurationStore = configurationStoreFactory
      .withType(PluginCenterAuthenticator.Authentication.class)
      .withName("plugin-center-auth")
      .build();
    configurationStore.getOptional()
      .ifPresent(config -> {
        String token = config.getRefreshToken();
        CipherUtil cipher = CipherUtil.getInstance();
        if (Strings.isNullOrEmpty(token) || !token.startsWith("{enc}")) {
          token = "{enc}".concat(cipher.encode(token));
          config.setRefreshToken(token);
          configurationStore.set(config);
        }
      });
  }


  @Override
  public Version getTargetVersion() {
    return parse("2.30.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.plugin-center.authentication";
  }
}
