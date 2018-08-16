// @flow
import React from "react";
import { translate } from "react-i18next";
import { SubmitButton } from "../../../components/buttons/index";
import type { Config } from "../../types/Config";
import ProxySettings from "./ProxySettings";
import GeneralSettings from "./GeneralSettings";
import BaseUrlSettings from "./BaseUrlSettings";
import AdminSettings from "./AdminSettings";

type Props = {
  submitForm: Config => void,
  config?: Config,
  loading?: boolean,
  t: string => string,
  configUpdatePermission: boolean
};

type State = {
  config: Config
};

class ConfigForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      config: {
        proxyPassword: null,
        proxyPort: 0,
        proxyServer: "",
        proxyUser: null,
        enableProxy: false,
        realmDescription: "",
        enableRepositoryArchive: false,
        disableGroupingGrid: false,
        dateFormat: "",
        anonymousAccessEnabled: false,
        adminGroups: [],
        adminUsers: [],
        baseUrl: "",
        forceBaseUrl: false,
        loginAttemptLimit: 0,
        proxyExcludes: [],
        skipFailedAuthenticators: false,
        pluginUrl: "",
        loginAttemptLimitTimeout: 0,
        enabledXsrfProtection: true,
        defaultNamespaceStrategy: "",
        _links: {}
      }
    };
  }

  componentDidMount() {
    const { config } = this.props;
    console.log(config);
    if (config) {
      this.setState({ config: { ...config } });
    }
  }

  submit = (event: Event) => {
    event.preventDefault();
    this.props.submitForm(this.state.config);
  };

  render() {
    const { loading, t, configUpdatePermission } = this.props;
    let config = this.state.config;
    return (
      <form onSubmit={this.submit}>
        <GeneralSettings
          realmDescription={config.realmDescription}
          enableRepositoryArchive={config.enableRepositoryArchive}
          disableGroupingGrid={config.disableGroupingGrid}
          dateFormat={config.dateFormat}
          anonymousAccessEnabled={config.anonymousAccessEnabled}
          loginAttemptLimit={config.loginAttemptLimit}
          skipFailedAuthenticators={config.skipFailedAuthenticators}
          pluginUrl={config.pluginUrl}
          loginAttemptLimitTimeout={config.loginAttemptLimitTimeout}
          enabledXsrfProtection={config.enabledXsrfProtection}
          defaultNamespaceStrategy={config.defaultNamespaceStrategy}
          onChange={(isValid, changedValue, name) =>
            this.onChange(isValid, changedValue, name)
          }
          hasUpdatePermission={configUpdatePermission}
        />
        <hr />
        <BaseUrlSettings
          baseUrl={config.baseUrl}
          forceBaseUrl={config.forceBaseUrl}
          onChange={(isValid, changedValue, name) =>
            this.onChange(isValid, changedValue, name)
          }
          hasUpdatePermission={configUpdatePermission}
        />
        <hr />
        <AdminSettings
          adminGroups={config.adminGroups}
          adminUsers={config.adminUsers}
          onChange={(isValid, changedValue, name) =>
            this.onChange(isValid, changedValue, name)
          }
          hasUpdatePermission={configUpdatePermission}
        />
        <hr />
        <ProxySettings
          proxyPassword={config.proxyPassword ? config.proxyPassword : ""}
          proxyPort={config.proxyPort}
          proxyServer={config.proxyServer ? config.proxyServer : ""}
          proxyUser={config.proxyUser ? config.proxyUser : ""}
          enableProxy={config.enableProxy}
          proxyExcludes={config.proxyExcludes}
          onChange={(isValid, changedValue, name) =>
            this.onChange(isValid, changedValue, name)
          }
          hasUpdatePermission={configUpdatePermission}
        />
        <hr />
        <SubmitButton
          // disabled={!this.isValid()}
          loading={loading}
          label={t("config-form.submit")}
          disabled={!configUpdatePermission}
        />
      </form>
    );
  }

  onChange = (isValid: boolean, changedValue: any, name: string) => {
    if (isValid) {
      this.setState({
        config: {
          ...this.state.config,
          [name]: changedValue
        }
      });
    }
  };
}

export default translate("config")(ConfigForm);
