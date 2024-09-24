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
        helpText={t("scm-hg-plugin.config.hgBinaryHelpText")}
        value={configuration.hgBinary}
        onChange={value => setConfiguration({ ...configuration, hgBinary: value })}
        validationError={hasValidationError("hgBinary")}
        errorMessage={t("scm-hg-plugin.config.required")}
        disabled={readOnly}
      />
      <InputField
        name="encoding"
        label={t("scm-hg-plugin.config.encoding")}
        helpText={t("scm-hg-plugin.config.encodingHelpText")}
        value={configuration.encoding}
        onChange={value => setConfiguration({ ...configuration, encoding: value })}
        validationError={hasValidationError("encoding")}
        errorMessage={t("scm-hg-plugin.config.required")}
        disabled={readOnly}
      />
      <Checkbox
        name="showRevisionInId"
        label={t("scm-hg-plugin.config.showRevisionInId")}
        helpText={t("scm-hg-plugin.config.showRevisionInIdHelpText")}
        checked={configuration.showRevisionInId}
        onChange={value => setConfiguration({ ...configuration, showRevisionInId: value })}
        disabled={readOnly}
      />
      <Checkbox
        name="enableHttpPostArgs"
        label={t("scm-hg-plugin.config.enableHttpPostArgs")}
        helpText={t("scm-hg-plugin.config.enableHttpPostArgsHelpText")}
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
