// @flow
import React from "react";
import { translate } from "react-i18next";
import { Title, Loading, ErrorNotification } from "@scm-manager/ui-components";
import {
  fetchConfig,
  getFetchConfigFailure,
  isFetchConfigPending,
  getConfig,
  modifyConfig,
  isModifyConfigPending,
  getConfigUpdatePermission,
  getModifyConfigFailure,
  modifyConfigReset
} from "../modules/config";
import { connect } from "react-redux";
import type { Config, NamespaceStrategies } from "@scm-manager/ui-types";
import ConfigForm from "../components/form/ConfigForm";
import { getConfigLink } from "../../modules/indexResource";
import {
  fetchNamespaceStrategiesIfNeeded,
  getFetchNamespaceStrategiesFailure,
  getNamespaceStrategies,
  isFetchNamespaceStrategiesPending
} from "../modules/namespaceStrategies";

type Props = {
  loading: boolean,
  error: Error,
  config: Config,
  configUpdatePermission: boolean,
  configLink: string,
  namespaceStrategies?: NamespaceStrategies,

  // dispatch functions
  modifyConfig: (config: Config, callback?: () => void) => void,
  fetchConfig: (link: string) => void,
  configReset: void => void,
  fetchNamespaceStrategiesIfNeeded: void => void,

  // context objects
  t: string => string
};

type State = {
  configReadPermission: boolean,
  configChanged: boolean
};

class GlobalConfig extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      configReadPermission: true,
      configChanged: false
    };
  }

  componentDidMount() {
    this.props.configReset();
    this.props.fetchNamespaceStrategiesIfNeeded();
    if (this.props.configLink) {
      this.props.fetchConfig(this.props.configLink);
    } else {
      this.setState({configReadPermission: false});
    }
  }

  modifyConfig = (config: Config) => {
    this.props.modifyConfig(config);
    this.setState({ configChanged: true });
  };

  renderConfigChangedNotification = () => {
    if (this.state.configChanged) {
      return (
        <div className="notification is-primary">
          <button
            className="delete"
            onClick={() => this.setState({ configChanged: false })}
          />
          {this.props.t("config-form.submit-success-notification")}
        </div>
      );
    }
    return null;
  };

  render() {
    const { t, loading } = this.props;

    if (loading) {
      return <Loading />;
    }

    return (
      <div>
        <Title title={t("config.title")} />
        {this.renderError()}
        {this.renderContent()}
      </div>
    );
  }

  renderError = () => {
    const { error } = this.props;
    if (error) {
      return <ErrorNotification error={error} />;
    }
    return null;
  };

  renderContent = () => {
    const { error, loading, config, configUpdatePermission, namespaceStrategies } = this.props;
    const { configReadPermission } = this.state;
    if (!error) {
      return (
        <>
          {this.renderConfigChangedNotification()}
          <ConfigForm
            submitForm={config => this.modifyConfig(config)}
            config={config}
            loading={loading}
            namespaceStrategies={namespaceStrategies}
            configUpdatePermission={configUpdatePermission}
            configReadPermission={configReadPermission}
          />
        </>
      );
    }
    return null;
  };
}

const mapDispatchToProps = dispatch => {
  return {
    fetchConfig: (link: string) => {
      dispatch(fetchConfig(link));
    },
    modifyConfig: (config: Config, callback?: () => void) => {
      dispatch(modifyConfig(config, callback));
    },
    configReset: () => {
      dispatch(modifyConfigReset());
    },
    fetchNamespaceStrategiesIfNeeded: () => {
      dispatch(fetchNamespaceStrategiesIfNeeded());
    }
  };
};

const mapStateToProps = state => {
  const loading = isFetchConfigPending(state)
    || isModifyConfigPending(state)
    || isFetchNamespaceStrategiesPending(state);
  const error = getFetchConfigFailure(state)
    || getModifyConfigFailure(state)
    || getFetchNamespaceStrategiesFailure(state);

  const config = getConfig(state);
  const configUpdatePermission = getConfigUpdatePermission(state);
  const configLink = getConfigLink(state);
  const namespaceStrategies = getNamespaceStrategies(state);

  return {
    loading,
    error,
    config,
    configUpdatePermission,
    configLink,
    namespaceStrategies
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("config")(GlobalConfig));
