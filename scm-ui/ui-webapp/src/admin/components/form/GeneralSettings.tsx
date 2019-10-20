import React from "react";
import { translate } from "react-i18next";
import { Checkbox, InputField } from "@scm-manager/ui-components";
import { NamespaceStrategies } from "@scm-manager/ui-types";
import NamespaceStrategySelect from "./NamespaceStrategySelect";

type Props = {
  realmDescription: string;
  loginInfoUrl: string;
  disableGroupingGrid: boolean;
  dateFormat: string;
  anonymousAccessEnabled: boolean;
  skipFailedAuthenticators: boolean;
  pluginUrl: string;
  enabledXsrfProtection: boolean;
  namespaceStrategy: string;
  namespaceStrategies?: NamespaceStrategies;
  onChange: (p1: boolean, p2: any, p3: string) => void;
  hasUpdatePermission: boolean;
  // context props
  t: (p: string) => string;
};

class GeneralSettings extends React.Component<Props> {
  render() {
    const {
      t,
      realmDescription,
      loginInfoUrl,
      pluginUrl,
      enabledXsrfProtection,
      anonymousAccessEnabled,
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
              checked={enabledXsrfProtection}
              label={t("general-settings.enabled-xsrf-protection")}
              onChange={this.handleEnabledXsrfProtectionChange}
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
            <Checkbox
              checked={anonymousAccessEnabled}
              label={t("general-settings.anonymous-access-enabled")}
              onChange={this.handleEnableAnonymousAccess}
              disabled={!hasUpdatePermission}
              helpText={t("help.allowAnonymousAccessHelpText")}
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
  handleEnableAnonymousAccess = (value: boolean) => {
    this.props.onChange(true, value, "anonymousAccessEnabled");
  };
  handleNamespaceStrategyChange = (value: string) => {
    this.props.onChange(true, value, "namespaceStrategy");
  };
  handlePluginCenterUrlChange = (value: string) => {
    this.props.onChange(true, value, "pluginUrl");
  };
}

export default translate("config")(GeneralSettings);
