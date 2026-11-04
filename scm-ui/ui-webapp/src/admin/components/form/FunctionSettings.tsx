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
import { Checkbox, Field, Label, Subtitle } from "@scm-manager/ui-core";
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
          <Field>
            <Label as="span">{t("general-settings.enabled-user-converter")}</Label>
            <Checkbox
              label={t("general-settings.enabled-user-converter")}
              onChange={(event) => handleEnabledUserConverterChange(event.target.checked)}
              checked={enabledUserConverter}
              disabled={!hasUpdatePermission}
              helpText={t("help.enabledUserConverterHelpText")}
            />
          </Field>
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <Field>
            <Label as="span">{t("general-settings.enabled-api-keys")}</Label>
            <Checkbox
              label={t("general-settings.enabled-api-keys")}
              onChange={(event) => handleEnabledApiKeysChange(event.target.checked)}
              checked={enabledApiKeys}
              disabled={!hasUpdatePermission}
              helpText={t("help.enabledApiKeysHelpText")}
            />
          </Field>
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <Field>
            <Label as="span">{t("general-settings.enabled-file-search")}</Label>
            <Checkbox
              label={t("general-settings.enabled-file-search")}
              onChange={(event) => handleEnabledFileSearchChange(event.target.checked)}
              checked={enabledFileSearch}
              disabled={!hasUpdatePermission}
              helpText={t("help.enabledFileSearchHelpText")}
            />
          </Field>
        </div>
      </div>
    </>
  );
};

export default FunctionSettings;
