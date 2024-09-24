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

import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Checkbox, InputField, Subtitle } from "@scm-manager/ui-components";
import { ConfigChangeHandler } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  baseUrl: string;
  forceBaseUrl: boolean;
  onChange: ConfigChangeHandler;
  hasUpdatePermission: boolean;
};

class BaseUrlSettings extends React.Component<Props> {
  render() {
    const { t, baseUrl, forceBaseUrl, hasUpdatePermission } = this.props;

    return (
      <>
        <Subtitle subtitle={t("base-url-settings.name")} />
        <div className="columns">
          <div className="column">
            <InputField
              label={t("base-url-settings.base-url")}
              onChange={this.handleBaseUrlChange}
              value={baseUrl}
              disabled={!hasUpdatePermission}
              helpText={t("help.baseUrlHelpText")}
            />
            <Checkbox
              checked={forceBaseUrl}
              label={t("base-url-settings.force-base-url")}
              onChange={this.handleForceBaseUrlChange}
              disabled={!hasUpdatePermission}
              helpText={t("help.forceBaseUrlHelpText")}
            />
          </div>
        </div>
      </>
    );
  }

  handleBaseUrlChange = (value: string) => {
    this.props.onChange(true, value, "baseUrl");
  };
  handleForceBaseUrlChange = (value: boolean) => {
    this.props.onChange(true, value, "forceBaseUrl");
  };
}

export default withTranslation("config")(BaseUrlSettings);
