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

import React, { FC, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Links } from "@scm-manager/ui-types";
import { Checkbox, Select } from "@scm-manager/ui-components";

type Configuration = {
  disabled: boolean;
  allowDisable: boolean;
  compatibility: string;
  enabledGZip: boolean;
  _links: Links;
};

type Props = {
  initialConfiguration: Configuration;
  readOnly: boolean;
  onConfigurationChange: (p1: Configuration, p2: boolean) => void;
};

const SvnConfigurationForm: FC<Props> = ({ initialConfiguration, readOnly, onConfigurationChange }) => {
  const [t] = useTranslation("plugins");
  const [configuration, setConfiguration] = useState(initialConfiguration);

  useEffect(() => setConfiguration(initialConfiguration), [initialConfiguration]);
  useEffect(() => onConfigurationChange(configuration, true), [configuration]);

  const options = ["NONE", "PRE14", "PRE15", "PRE16", "PRE17", "WITH17"].map((option: string) => ({
    value: option,
    label: t("scm-svn-plugin.config.compatibility-values." + option.toLowerCase())
  }));

  return (
    <>
      <Select
        name="compatibility"
        label={t("scm-svn-plugin.config.compatibility")}
        helpText={t("scm-svn-plugin.config.compatibilityHelpText")}
        value={configuration.compatibility}
        options={options}
        onChange={option => setConfiguration({ ...configuration, compatibility: option })}
      />
      <Checkbox
        name="enabledGZip"
        label={t("scm-svn-plugin.config.enabledGZip")}
        helpText={t("scm-svn-plugin.config.enabledGZipHelpText")}
        checked={configuration.enabledGZip}
        onChange={value => setConfiguration({ ...configuration, enabledGZip: value })}
        disabled={readOnly}
      />
      <Checkbox
        name="disabled"
        label={t("scm-svn-plugin.config.disabled")}
        helpText={t("scm-svn-plugin.config.disabledHelpText")}
        checked={configuration.disabled}
        onChange={value => setConfiguration({ ...configuration, disabled: value })}
        disabled={readOnly || !configuration.allowDisable}
      />
    </>
  );
};

export default SvnConfigurationForm;
