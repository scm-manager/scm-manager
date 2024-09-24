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
            helpText={t("help.enabledFileSearchHelpText")}
          />
        </div>
      </div>
    </>
  );
};

export default FunctionSettings;
