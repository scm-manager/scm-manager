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
import { Checkbox, ChipInputField, Field, InputField, Subtitle } from "@scm-manager/ui-core";
import { ConfigChangeHandler } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  proxyPassword: string;
  proxyPort: number;
  proxyServer: string;
  proxyUser: string;
  enableProxy: boolean;
  proxyExcludes: string[];
  onChange: ConfigChangeHandler;
  hasUpdatePermission: boolean;
};

class ProxySettings extends React.Component<Props> {
  render() {
    const { t, proxyPassword, proxyPort, proxyServer, proxyUser, enableProxy, proxyExcludes, hasUpdatePermission } =
      this.props;

    return (
      <div>
        <Subtitle subtitle={t("proxySettings.subtitle")} />
        <div className="columns">
          <div className="column is-full">
            <Field>
              <Checkbox
                checked={enableProxy}
                label={t("proxySettings.enable")}
                onChange={(event) => this.handleEnableProxyChange(event.target.checked)}
                disabled={!hasUpdatePermission}
                helpText={t("proxySettings.enableHelpText")}
              />
            </Field>
          </div>
        </div>
        <div className="columns">
          <div className="column is-full">
            <InputField
              label={t("proxySettings.server")}
              value={proxyServer}
              onChange={(event) => this.handleProxyServerChange(event.target.value)}
              disabled={!enableProxy || !hasUpdatePermission}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-full">
            <InputField
              label={t("proxySettings.port")}
              value={proxyPort}
              onChange={(event) => this.handleProxyPortChange(event.target.value)}
              disabled={!enableProxy || !hasUpdatePermission}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-full">
            <InputField
              label={t("proxySettings.user")}
              value={proxyUser}
              onChange={(event) => this.handleProxyUserChange(event.target.value)}
              disabled={!enableProxy || !hasUpdatePermission}
              descriptionText={t("proxySettings.userHelpText")}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column">
            <InputField
              label={t("proxySettings.password")}
              onChange={(event) => this.handleProxyPasswordChange(event.target.value)}
              value={proxyPassword}
              type="password"
              disabled={!enableProxy || !hasUpdatePermission}
              descriptionText={t("proxySettings.passwordHelpText")}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-full">
            <ChipInputField<string>
              label={t("proxySettings.excludesTable.label")}
              information={t("proxySettings.excludesTable.helpText")}
              placeholder={t("proxySettings.addExclude")}
              value={proxyExcludes.map((proxyExclude) => ({ label: proxyExclude, value: proxyExclude }))}
              onChange={(excludes) => this.handleProxyExcludesChange(excludes.map((exclude) => exclude.value))}
              disabled={!enableProxy || !hasUpdatePermission}
            />
          </div>
        </div>
      </div>
    );
  }

  handleProxyPasswordChange = (value: string) => {
    this.props.onChange(true, value, "proxyPassword");
  };
  handleProxyPortChange = (value: string) => {
    this.props.onChange(true, Number(value), "proxyPort");
  };
  handleProxyServerChange = (value: string) => {
    this.props.onChange(true, value, "proxyServer");
  };
  handleProxyUserChange = (value: string) => {
    this.props.onChange(true, value, "proxyUser");
  };
  handleEnableProxyChange = (value: boolean) => {
    this.props.onChange(true, value, "enableProxy");
  };

  handleProxyExcludesChange = (proxyExcludes: string[]) => {
    this.props.onChange(true, proxyExcludes, "proxyExcludes");
  };
}

export default withTranslation("config")(ProxySettings);
