// @flow
import React from "react";
import { translate } from "react-i18next";
import { Checkbox, InputField } from "@scm-manager/ui-components";
import type { NamespaceStrategies } from "@scm-manager/ui-types";
import NamespaceStrategySelect from "./NamespaceStrategySelect";

type Props = {
  realmDescription: string,
  enableRepositoryArchive: boolean,
  disableGroupingGrid: boolean,
  dateFormat: string,
  anonymousAccessEnabled: boolean,
  skipFailedAuthenticators: boolean,
  pluginUrl: string,
  enabledXsrfProtection: boolean,
  namespaceStrategy: string,
  namespaceStrategies?: NamespaceStrategies,
  onChange: (boolean, any, string) => void,
  hasUpdatePermission: boolean,
  // context props
  t: string => string
};

class GeneralSettings extends React.Component<Props> {
  render() {
    const {
      t,
      realmDescription,
      enabledXsrfProtection,
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
            <Checkbox
              checked={enabledXsrfProtection}
              label={t("general-settings.enabled-xsrf-protection")}
              onChange={this.handleEnabledXsrfProtectionChange}
              disabled={!hasUpdatePermission}
              helpText={t("help.enableXsrfProtectionHelpText")}
            />
          </div>
        </div>
      </div>
    );
  }

  handleRealmDescriptionChange = (value: string) => {
    this.props.onChange(true, value, "realmDescription");
  };
  handleEnabledXsrfProtectionChange = (value: boolean) => {
    this.props.onChange(true, value, "enabledXsrfProtection");
  };
  handleNamespaceStrategyChange = (value: string) => {
    this.props.onChange(true, value, "namespaceStrategy");
  };
}

export default translate("config")(GeneralSettings);
