// @flow
import React from "react";
import { translate } from "react-i18next";
import { SubmitButton } from "../../../components/buttons/index";
import type { Config } from "../../types/Config";
import ProxySettings from "./ProxySettings";
import GeneralSettings from "./GeneralSettings";
import BaseUrlSettings from "./BaseUrlSettings";
import AdminSettings from "./AdminSettings";
import Notification from "../../../components/Notification";
import LoginAttempt from "./LoginAttempt";

type Props = {
  submitForm: Config => void,
  config?: Config,
  loading?: boolean,
  t: string => string,
  configUpdatePermission: boolean
};

type State = {
  config: Config,
  showNotification: boolean,
  error: {
    loginAttemptLimitTimeout: boolean,
    loginAttemptLimit: boolean
  }
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
      },
      showNotification: false,
      error: {
        loginAttemptLimitTimeout: false,
        loginAttemptLimit: false
      }
    };
  }

  componentDidMount() {
    const { config, configUpdatePermission } = this.props;
    if (config) {
      this.setState({ ...this.state, config: { ...config } });
    }
    if (!configUpdatePermission) {
      this.setState({ ...this.state, showNotification: true });
    }
  }

  submit = (event: Event) => {
    event.preventDefault();
    this.props.submitForm(this.state.config);
  };

  render() {
    const { loading, t, configUpdatePermission } = this.props;
    const config = this.state.config;

    let noPermissionNotification = null;

    if (this.state.showNotification) {
      noPermissionNotification = (
        <Notification
          type={"info"}
          children={t("config-form.no-permission-notification")}
          onClose={() => this.onClose()}
        />
      );
    }

    return (
      <form onSubmit={this.submit}>
        {noPermissionNotification}
        <GeneralSettings
          realmDescription={config.realmDescription}
          enableRepositoryArchive={config.enableRepositoryArchive}
          disableGroupingGrid={config.disableGroupingGrid}
          dateFormat={config.dateFormat}
          anonymousAccessEnabled={config.anonymousAccessEnabled}
          skipFailedAuthenticators={config.skipFailedAuthenticators}
          pluginUrl={config.pluginUrl}
          enabledXsrfProtection={config.enabledXsrfProtection}
          defaultNamespaceStrategy={config.defaultNamespaceStrategy}
          onChange={(isValid, changedValue, name) =>
            this.onChange(isValid, changedValue, name)
          }
          hasUpdatePermission={configUpdatePermission}
        />
        <hr />
        <LoginAttempt
          loginAttemptLimit={config.loginAttemptLimit}
          loginAttemptLimitTimeout={config.loginAttemptLimitTimeout}
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
          loading={loading}
          label={t("config-form.submit")}
          disabled={!configUpdatePermission || this.hasError()}
        />
      </form>
    );
  }

  onChange = (isValid: boolean, changedValue: any, name: string) => {
    this.setState({
      ...this.state,
      config: {
        ...this.state.config,
        [name]: changedValue
      },
      error: {
        ...this.state.error,
        [name]: !isValid
      }
    });
  };

  hasError = () => {
    console.log("loginAttemtLimit " + this.state.error.loginAttemptLimit);
    console.log(
      "loginAttemtLimitTimeout " + this.state.error.loginAttemptLimitTimeout
    );

    console.log(
      this.state.error.loginAttemptLimit ||
        this.state.error.loginAttemptLimitTimeout
    );
    return (
      this.state.error.loginAttemptLimit ||
      this.state.error.loginAttemptLimitTimeout
    );
  };

  onClose = () => {
    this.setState({
      ...this.state,
      showNotification: false
    });
  };
}

export default translate("config")(ConfigForm);
