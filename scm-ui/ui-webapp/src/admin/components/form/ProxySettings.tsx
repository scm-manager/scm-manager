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
          <div className="column is-half">
            <InputField
              label={t("proxySettings.server")}
              value={proxyServer}
              onChange={this.handleProxyServerChange}
              disabled={!enableProxy || !hasUpdatePermission}
              helpText={t("proxySettings.serverHelpText")}
            />
          </div>
          <div className="column is-half">
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
          <div className="column is-half">
            <InputField
              label={t("proxySettings.user")}
              value={proxyUser}
              onChange={this.handleProxyUserChange}
              disabled={!enableProxy || !hasUpdatePermission}
              helpText={t("proxySettings.userHelpText")}
            />
          </div>
          <div className="column is-half">
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
