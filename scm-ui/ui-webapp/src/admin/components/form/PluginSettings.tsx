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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { InputField, Subtitle } from "@scm-manager/ui-components";
import PluginCenterAuthentication from "./PluginCenterAuthentication";

type Props = {
  pluginUrl: string;
  pluginAuthUrl: string;
  onChange: (isValid: boolean, changedValue: string, name: string) => void;
  hasUpdatePermission: boolean;
};

const PluginSettings: FC<Props> = ({ pluginUrl, pluginAuthUrl, onChange, hasUpdatePermission }) => {
  const { t } = useTranslation("config");

  const handlePluginCenterUrlChange = (value: string) => {
    onChange(true, value, "pluginUrl");
  };

  const handlePluginCenterAuthUrlChange = (value: string) => {
    onChange(true, value, "pluginAuthUrl");
  };

  return (
    <div>
      <Subtitle subtitle={t("pluginSettings.subtitle")} />
      <div className="columns">
        <div className="column">
          <InputField
            label={t("pluginSettings.pluginUrl")}
            onChange={handlePluginCenterUrlChange}
            value={pluginUrl}
            disabled={!hasUpdatePermission}
            helpText={t("help.pluginUrlHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <InputField
            label={t("pluginSettings.pluginAuthUrl")}
            onChange={handlePluginCenterAuthUrlChange}
            value={pluginAuthUrl}
            disabled={!hasUpdatePermission}
            helpText={t("help.pluginAuthUrlHelpText")}
          />
        </div>
      </div>
      <PluginCenterAuthentication />
    </div>
  );
};

export default PluginSettings;
