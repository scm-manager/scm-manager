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
import { Checkbox, InputField, Select } from "@scm-manager/ui-components";
import { NamespaceStrategies, AnonymousMode } from "@scm-manager/ui-types";
import NamespaceStrategySelect from "./NamespaceStrategySelect";

type Props = WithTranslation & {
  realmDescription: string;
  loginInfoUrl: string;
  disableGroupingGrid: boolean;
  dateFormat: string;
  anonymousMode: AnonymousMode;
  skipFailedAuthenticators: boolean;
  pluginUrl: string;
  releaseFeedUrl: string;
  mailDomainName: string;
  enabledXsrfProtection: boolean;
  enabledUserConverter: boolean;
  enabledApiKeys: boolean;
  namespaceStrategy: string;
  namespaceStrategies?: NamespaceStrategies;
  onChange: (p1: boolean, p2: any, p3: string) => void;
  hasUpdatePermission: boolean;
};

class GeneralSettings extends React.Component<Props> {
  render() {
    const {
      t,
      realmDescription,
      loginInfoUrl,
      pluginUrl,
      releaseFeedUrl,
      mailDomainName,
      enabledXsrfProtection,
      enabledUserConverter,
      enabledApiKeys,
      anonymousMode,
      namespaceStrategy,
      hasUpdatePermission,
      namespaceStrategies
    } = this.props;

    return (
      <div>
        <div className="columns">
          <div className="column is-half">
            <InputField
              label={t("general-settings.realm-description")}
              onChange={this.handleRealmDescriptionChange}
              value={realmDescription}
              disabled={!hasUpdatePermission}
              helpText={t("help.realmDescriptionHelpText")}
            />
          </div>
          <div className="column is-half">
            <NamespaceStrategySelect
              label={t("general-settings.namespace-strategy")}
              onChange={this.handleNamespaceStrategyChange}
              value={namespaceStrategy}
              disabled={!hasUpdatePermission}
              namespaceStrategies={namespaceStrategies}
              helpText={t("help.nameSpaceStrategyHelpText")}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-half">
            <InputField
              label={t("general-settings.login-info-url")}
              onChange={this.handleLoginInfoUrlChange}
              value={loginInfoUrl}
              disabled={!hasUpdatePermission}
              helpText={t("help.loginInfoUrlHelpText")}
            />
          </div>
          <div className="column is-half">
            <Checkbox
              label={t("general-settings.enabled-xsrf-protection")}
              onChange={this.handleEnabledXsrfProtectionChange}
              checked={enabledXsrfProtection}
              title={t("general-settings.enabled-xsrf-protection")}
              disabled={!hasUpdatePermission}
              helpText={t("help.enableXsrfProtectionHelpText")}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-half">
            <InputField
              label={t("general-settings.plugin-url")}
              onChange={this.handlePluginCenterUrlChange}
              value={pluginUrl}
              disabled={!hasUpdatePermission}
              helpText={t("help.pluginUrlHelpText")}
            />
          </div>
          <div className="column is-half">
            <Select
              label={t("general-settings.anonymousMode.title")}
              onChange={this.handleAnonymousMode}
              value={anonymousMode}
              disabled={!hasUpdatePermission}
              options={[
                { label: t("general-settings.anonymousMode.full"), value: "FULL" },
                { label: t("general-settings.anonymousMode.protocolOnly"), value: "PROTOCOL_ONLY" },
                { label: t("general-settings.anonymousMode.off"), value: "OFF" }
              ]}
              helpText={t("help.allowAnonymousAccessHelpText")}
              testId={"anonymous-mode-select"}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-half">
            <InputField
              label={t("general-settings.release-feed-url")}
              onChange={this.handleReleaseFeedUrlChange}
              value={releaseFeedUrl}
              disabled={!hasUpdatePermission}
              helpText={t("help.releaseFeedUrlHelpText")}
            />
          </div>
          <div className="column is-half">
            <Checkbox
              label={t("general-settings.enabled-user-converter")}
              onChange={this.handleEnabledUserConverterChange}
              checked={enabledUserConverter}
              title={t("general-settings.enabled-user-converter")}
              disabled={!hasUpdatePermission}
              helpText={t("help.enabledUserConverterHelpText")}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-half">
            <InputField
              label={t("general-settings.mail-domain-name")}
              onChange={this.handleMailDomainNameChange}
              value={mailDomainName}
              disabled={!hasUpdatePermission}
              helpText={t("help.mailDomainNameHelpText")}
            />
          </div>
          <div className="column is-half">
            <Checkbox
              label={t("general-settings.enabled-api-keys")}
              onChange={this.handleEnabledApiKeysChange}
              checked={enabledApiKeys}
              title={t("general-settings.enabled-api-keys")}
              disabled={!hasUpdatePermission}
              helpText={t("help.enabledApiKeysHelpText")}
            />
          </div>
        </div>
      </div>
    );
  }

  handleLoginInfoUrlChange = (value: string) => {
    this.props.onChange(true, value, "loginInfoUrl");
  };
  handleRealmDescriptionChange = (value: string) => {
    this.props.onChange(true, value, "realmDescription");
  };
  handleEnabledXsrfProtectionChange = (value: boolean) => {
    this.props.onChange(true, value, "enabledXsrfProtection");
  };
  handleEnabledUserConverterChange = (value: boolean) => {
    this.props.onChange(true, value, "enabledUserConverter");
  };
  handleAnonymousMode = (value: string) => {
    this.props.onChange(true, value, "anonymousMode");
  };
  handleNamespaceStrategyChange = (value: string) => {
    this.props.onChange(true, value, "namespaceStrategy");
  };
  handlePluginCenterUrlChange = (value: string) => {
    this.props.onChange(true, value, "pluginUrl");
  };
  handleReleaseFeedUrlChange = (value: string) => {
    this.props.onChange(true, value, "releaseFeedUrl");
  };
  handleMailDomainNameChange = (value: string) => {
    this.props.onChange(true, value, "mailDomainName");
  };
  handleEnabledApiKeysChange = (value: boolean) => {
    this.props.onChange(true, value, "enabledApiKeys");
  };
}

export default withTranslation("config")(GeneralSettings);
