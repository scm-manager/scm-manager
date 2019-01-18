// @flow
import React from "react";
import { translate } from "react-i18next";
import { Title, ErrorPage, Loading } from "@scm-manager/ui-components";
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
import type { Config } from "@scm-manager/ui-types";
import ConfigForm from "../components/form/ConfigForm";
import { getConfigLink } from "../../modules/indexResource";

type Props = {
  loading: boolean,
  error: Error,
  config: Config,
  configUpdatePermission: boolean,
  configLink: string,

  // dispatch functions
  modifyConfig: (config: Config, callback?: () => void) => void,
  fetchConfig: (link: string) => void,
  configReset: void => void,

  // context objects
  t: string => string
};

type State = {
  configChanged: boolean
};

class GlobalConfig extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      configChanged: false
    };
  }

  componentDidMount() {
    this.props.configReset();
    this.props.fetchConfig(this.props.configLink);
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
    const { t, error, loading, config, configUpdatePermission } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("config.errorTitle")}
          subtitle={t("config.errorSubtitle")}
          error={error}
          configUpdatePermission={configUpdatePermission}
        />
      );
    }
    if (loading) {
      return <Loading />;
    }

    return (
      <div>
        <Title title={t("config.title")} />
        {this.renderConfigChangedNotification()}
        <ConfigForm
          submitForm={config => this.modifyConfig(config)}
          config={config}
          loading={loading}
          configUpdatePermission={configUpdatePermission}
        />
      </div>
    );
  }
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
    }
  };
};

const mapStateToProps = state => {
  const loading = isFetchConfigPending(state) || isModifyConfigPending(state);
  const error = getFetchConfigFailure(state) || getModifyConfigFailure(state);
  const config = getConfig(state);
  const configUpdatePermission = getConfigUpdatePermission(state);
  const configLink = getConfigLink(state);

  return {
    loading,
    error,
    config,
    configUpdatePermission,
    configLink
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("config")(GlobalConfig));
