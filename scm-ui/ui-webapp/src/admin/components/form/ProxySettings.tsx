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
import { AddEntryToTableField, Checkbox, InputField, Subtitle } from "@scm-manager/ui-components";
import ProxyExcludesTable from "../table/ProxyExcludesTable";
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
            <Checkbox
              checked={enableProxy}
              label={t("proxySettings.enable")}
              onChange={this.handleEnableProxyChange}
              disabled={!hasUpdatePermission}
              helpText={t("proxySettings.enableHelpText")}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-full">
            <InputField
              label={t("proxySettings.server")}
              value={proxyServer}
              onChange={this.handleProxyServerChange}
              disabled={!enableProxy || !hasUpdatePermission}
              helpText={t("proxySettings.serverHelpText")}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-full">
            <InputField
              label={t("proxySettings.port")}
              value={proxyPort}
              onChange={this.handleProxyPortChange}
              disabled={!enableProxy || !hasUpdatePermission}
              helpText={t("proxySettings.portHelpText")}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-full">
            <InputField
              label={t("proxySettings.user")}
              value={proxyUser}
              onChange={this.handleProxyUserChange}
              disabled={!enableProxy || !hasUpdatePermission}
              helpText={t("proxySettings.userHelpText")}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column">
            <InputField
              label={t("proxySettings.password")}
              onChange={this.handleProxyPasswordChange}
              value={proxyPassword}
              type="password"
              disabled={!enableProxy || !hasUpdatePermission}
              helpText={t("proxySettings.passwordHelpText")}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-full">
            <ProxyExcludesTable
              proxyExcludes={proxyExcludes}
              onChange={this.props.onChange}
              disabled={!enableProxy || !hasUpdatePermission}
            />
            <AddEntryToTableField
              addEntry={this.addProxyExclude}
              disabled={!enableProxy || !hasUpdatePermission}
              buttonLabel={t("proxySettings.addExcludeButton")}
              fieldLabel={t("proxySettings.addExclude")}
              helpText={t("proxySettings.excludesTable.helpText")}
              errorMessage={t("proxySettings.addExcludeError")}
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

  addProxyExclude = (proxyExcludeName: string) => {
    if (this.isProxyExcludeMember(proxyExcludeName)) {
      return;
    }
    this.props.onChange(true, [...this.props.proxyExcludes, proxyExcludeName], "proxyExcludes");
  };

  isProxyExcludeMember = (proxyExcludeName: string) => {
    return this.props.proxyExcludes.includes(proxyExcludeName);
  };
}

export default withTranslation("config")(ProxySettings);
