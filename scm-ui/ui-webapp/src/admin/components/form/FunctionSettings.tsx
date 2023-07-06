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
import { ConfigChangeHandler } from "@scm-manager/ui-types";
import { Checkbox, Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  enabledUserConverter: boolean;
  enabledApiKeys: boolean;
  enabledFileSearch: boolean;
  onChange: ConfigChangeHandler;
  hasUpdatePermission: boolean;
};

const FunctionSettings: FC<Props> = ({
  enabledFileSearch,
  enabledUserConverter,
  enabledApiKeys,
  onChange,
  hasUpdatePermission,
}) => {
  const { t } = useTranslation("config");

  const handleEnabledApiKeysChange = (value: boolean) => {
    onChange(true, value, "enabledApiKeys");
  };
  const handleEnabledUserConverterChange = (value: boolean) => {
    onChange(true, value, "enabledUserConverter");
  };
  const handleEnabledFileSearchChange = (value: boolean) => {
    onChange(true, value, "enabledFileSearch");
  };

  return (
    <>
      <Subtitle subtitle={t("function-settings.name")} />
      <div className="columns">
        <div className="column">
          <Checkbox
            label={t("general-settings.enabled-user-converter")}
            onChange={handleEnabledUserConverterChange}
            checked={enabledUserConverter}
            title={t("general-settings.enabled-user-converter")}
            disabled={!hasUpdatePermission}
            helpText={t("help.enabledUserConverterHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <Checkbox
            label={t("general-settings.enabled-api-keys")}
            onChange={handleEnabledApiKeysChange}
            checked={enabledApiKeys}
            title={t("general-settings.enabled-api-keys")}
            disabled={!hasUpdatePermission}
            helpText={t("help.enabledApiKeysHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <Checkbox
            label={t("general-settings.enabled-file-search")}
            onChange={handleEnabledFileSearchChange}
            checked={enabledFileSearch}
            title={t("general-settings.enabled-file-search")}
            disabled={!hasUpdatePermission}
            helpText={t("help.enabledFileSearch")}
          />
        </div>
      </div>
    </>
  );
};

export default FunctionSettings;
