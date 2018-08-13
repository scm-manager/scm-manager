// @flow
import React from "react";
import { translate } from "react-i18next";
import { Checkbox, InputField } from "../../../components/forms/index";

type Props = {
  realmDescription: string,
  enableRepositoryArchive: boolean,
  disableGroupingGrid: boolean,
  dateFormat: string,
  anonymousAccessEnabled: boolean,
  loginAttemptLimit: number,
  skipFailedAuthenticators: boolean,
  pluginUrl: string,
  loginAttemptLimitTimeout: number,
  enabledXsrfProtection: boolean,
  defaultNamespaceStrategy: string,
  t: string => string,
  onChange: (boolean, any, string) => void
};

class GeneralSettings extends React.Component<Props> {
  render() {
    const {
      t,
      realmDescription,
      enableRepositoryArchive,
      disableGroupingGrid,
      dateFormat,
      anonymousAccessEnabled,
      loginAttemptLimit,
      skipFailedAuthenticators,
      pluginUrl,
      loginAttemptLimitTimeout,
      enabledXsrfProtection,
      defaultNamespaceStrategy
    } = this.props;

    return (
      <div>
        <InputField
          label={t("general-settings.realm-description")}
          onChange={this.handleRealmDescriptionChange}
          value={realmDescription}
        />
        <Checkbox
          checked={enableRepositoryArchive}
          label={t("general-settings.enable-repository-archive")}
          onChange={this.handleEnableRepositoryArchiveChange}
        />
        <Checkbox
          checked={disableGroupingGrid}
          label={t("general-settings.disable-grouping-grid")}
          onChange={this.handleDisableGroupingGridChange}
        />
        <InputField
          label={t("general-settings.date-format")}
          onChange={this.handleDateFormatChange}
          value={dateFormat}
        />
        <Checkbox
          checked={anonymousAccessEnabled}
          label={t("general-settings.anonymous-access-enabled")}
          onChange={this.handleAnonymousAccessEnabledChange}
        />
        <InputField
          label={t("general-settings.login-attempt-limit")}
          onChange={this.handleLoginAttemptLimitChange}
          value={loginAttemptLimit}
        />
        <InputField
          label={t("general-settings.login-attempt-limit-timeout")}
          onChange={this.handleLoginAttemptLimitTimeoutChange}
          value={loginAttemptLimitTimeout}
        />
        <Checkbox
          checked={skipFailedAuthenticators}
          label={t("general-settings.skip-failed-authenticators")}
          onChange={this.handleSkipFailedAuthenticatorsChange}
        />
        <InputField
          label={t("general-settings.plugin-url")}
          onChange={this.handlePluginUrlChange}
          value={pluginUrl}
        />
        <Checkbox
          checked={enabledXsrfProtection}
          label={t("general-settings.enabled-xsrf-protection")}
          onChange={this.handleEnabledXsrfProtectionChange}
        />
        <InputField
          label={t("general-settings.default-namespace-strategy")}
          onChange={this.handleDefaultNamespaceStrategyChange}
          value={defaultNamespaceStrategy}
        />
      </div>
    );
  }

  handleRealmDescriptionChange = (value: string) => {
    this.props.onChange(true, value, "realmDescription");
  };
  handleEnableRepositoryArchiveChange = (value: boolean) => {
    this.props.onChange(true, value, "enableRepositoryArchive");
  };
  handleDisableGroupingGridChange = (value: boolean) => {
    this.props.onChange(true, value, "disableGroupingGrid");
  };
  handleDateFormatChange = (value: string) => {
    this.props.onChange(true, value, "dateFormat");
  };
  handleAnonymousAccessEnabledChange = (value: string) => {
    this.props.onChange(true, value, "anonymousAccessEnabled");
  };
  handleLoginAttemptLimitChange = (value: string) => {
    this.props.onChange(true, value, "loginAttemptLimit");
  };
  handleSkipFailedAuthenticatorsChange = (value: string) => {
    this.props.onChange(true, value, "skipFailedAuthenticators");
  };
  handlePluginUrlChange = (value: string) => {
    this.props.onChange(true, value, "pluginUrl");
  };
  handleLoginAttemptLimitTimeoutChange = (value: string) => {
    this.props.onChange(true, value, "loginAttemptLimitTimeout");
  };
  handleEnabledXsrfProtectionChange = (value: boolean) => {
    this.props.onChange(true, value, "enabledXsrfProtection");
  };
  handleDefaultNamespaceStrategyChange = (value: string) => {
    this.props.onChange(true, value, "defaultNamespaceStrategy");
  };
}

export default translate("config")(GeneralSettings);
