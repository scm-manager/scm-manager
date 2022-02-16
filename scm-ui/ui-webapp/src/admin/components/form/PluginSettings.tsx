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
        <div className="column is-half">
          <InputField
            label={t("pluginSettings.pluginUrl")}
            onChange={handlePluginCenterUrlChange}
            value={pluginUrl}
            disabled={!hasUpdatePermission}
            helpText={t("help.pluginUrlHelpText")}
          />
        </div>
        <div className="column is-half">
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
