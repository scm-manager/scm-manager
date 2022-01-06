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
