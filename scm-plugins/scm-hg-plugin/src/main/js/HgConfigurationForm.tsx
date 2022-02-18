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
import { Link, Links } from "@scm-manager/ui-types";
import { apiClient, Button, Checkbox, InputField } from "@scm-manager/ui-components";

type Configuration = {
  disabled: boolean;
  allowDisable: boolean;
  hgBinary: string;
  encoding: string;
  showRevisionInId: boolean;
  enableHttpPostArgs: boolean;
  _links: Links;
};

type Props = {
  initialConfiguration: Configuration;
  readOnly: boolean;
  onConfigurationChange: (p1: Configuration, p2: boolean) => void;
};

const HgConfigurationForm: FC<Props> = ({ initialConfiguration, onConfigurationChange, readOnly }) => {
  const [validationErrors, setValidationErrors] = useState<string[]>([]);
  const [configuration, setConfiguration] = useState(initialConfiguration);
  const [t] = useTranslation("plugins");

  useEffect(() => setConfiguration(initialConfiguration), [initialConfiguration]);
  useEffect(() => onConfigurationChange(configuration, updateValidationStatus()), [configuration]);

  const updateValidationStatus = () => {
    const errors = [];
    if (!configuration.hgBinary) {
      errors.push("hgBinary");
    }
    if (!configuration.encoding) {
      errors.push("encoding");
    }

    setValidationErrors(errors);
    return errors.length === 0;
  };

  const hasValidationError = (name: string) => {
    return validationErrors.indexOf(name) >= 0;
  };

  const triggerAutoConfigure = () => {
    apiClient
      .put(
        (initialConfiguration._links.autoConfiguration as Link).href,
        { ...initialConfiguration, hgBinary: configuration.hgBinary },
        "application/vnd.scmm-hgConfig+json;v=2"
      )
      .then(() =>
        apiClient
          .get((initialConfiguration._links.self as Link).href)
          .then(r => r.json())
          .then((config: Configuration) => setConfiguration({ ...configuration, hgBinary: config.hgBinary }))
      )
      .then(() => onConfigurationChange(configuration, updateValidationStatus()));
  };

  return (
    <div className="is-flex is-flex-direction-column">
      <InputField
        name="hgBinary"
        label={t("scm-hg-plugin.config.hgBinary")}
        helpText={t("scm-hg-plugin.config.hgBinary.HelpText")}
        value={configuration.hgBinary}
        onChange={value => setConfiguration({ ...configuration, hgBinary: value })}
        validationError={hasValidationError("hgBinary")}
        errorMessage={t("scm-hg-plugin.config.required")}
        disabled={readOnly}
      />
      <InputField
        name="encoding"
        label={t("scm-hg-plugin.config.encoding")}
        helpText={t("scm-hg-plugin.config.encoding.HelpText")}
        value={configuration.encoding}
        onChange={value => setConfiguration({ ...configuration, encoding: value })}
        validationError={hasValidationError("encoding")}
        errorMessage={t("scm-hg-plugin.config.required")}
        disabled={readOnly}
      />
      <Checkbox
        name="showRevisionInId"
        label={t("scm-hg-plugin.config.showRevisionInId")}
        helpText={t("scm-hg-plugin.config.showRevisionInId.HelpText")}
        checked={configuration.showRevisionInId}
        onChange={value => setConfiguration({ ...configuration, showRevisionInId: value })}
        disabled={readOnly}
      />
      <Checkbox
        name="enableHttpPostArgs"
        label={t("scm-hg-plugin.config.enableHttpPostArgs")}
        helpText={t("scm-hg-plugin.config.enableHttpPostArgs.HelpText")}
        checked={configuration.enableHttpPostArgs}
        onChange={value => setConfiguration({ ...configuration, enableHttpPostArgs: value })}
        disabled={readOnly}
      />
      <Checkbox
        name="disabled"
        label={t("scm-hg-plugin.config.disabled")}
        helpText={t("scm-hg-plugin.config.disabledHelpText")}
        checked={configuration.disabled}
        onChange={value => {
          setConfiguration({ ...configuration, disabled: value });
        }}
        disabled={readOnly || !configuration.allowDisable}
      />
      <Button
        className="is-align-self-flex-end"
        disabled={!initialConfiguration?._links?.autoConfiguration}
        action={() => triggerAutoConfigure()}
      >
        {t("scm-hg-plugin.config.autoConfigure")}
      </Button>
    </div>
  );
};

export default HgConfigurationForm;
