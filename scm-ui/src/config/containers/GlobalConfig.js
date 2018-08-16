import React from "react";
import { translate } from "react-i18next";
import Title from "../../components/layout/Title";
import {
  fetchConfig,
  getFetchConfigFailure,
  isFetchConfigPending,
  getConfig,
  modifyConfig,
  isModifyConfigPending,
  getConfigUpdatePermission,
  getModifyConfigFailure
} from "../modules/config";
import connect from "react-redux/es/connect/connect";
import ErrorPage from "../../components/ErrorPage";
import type { Config } from "../types/Config";
import ConfigForm from "../components/form/ConfigForm";
import Loading from "../../components/Loading";
import type { User } from "../../users/types/User";
import type { History } from "history";

type Props = {
  loading: boolean,
  error: Error,
  config: Config,
  configUpdatePermission: boolean,
  // dispatch functions
  modifyConfig: (config: User, callback?: () => void) => void,
  // context objects
  t: string => string,
  fetchConfig: void => void,
  history: History
};

class GlobalConfig extends React.Component<Props> {
  configModified = (config: Config) => () => {
    this.props.fetchConfig();
    this.props.history.push(`/config`);
  };

  componentDidMount() {
    this.props.fetchConfig();
  }

  modifyConfig = (config: Config) => {
    console.log(config);
    this.props.modifyConfig(config, this.configModified(config));
  };

  render() {
    const { t, error, loading, config, configUpdatePermission } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("global-config.error-title")}
          subtitle={t("global-config.error-subtitle")}
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
        <Title title={t("global-config.title")} />
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
    fetchConfig: () => {
      dispatch(fetchConfig());
    },
    modifyConfig: (config: Config, callback?: () => void) => {
      dispatch(modifyConfig(config, callback));
    }
  };
};

const mapStateToProps = state => {
  const loading = isFetchConfigPending(state) || isModifyConfigPending(state); //TODO: Button lädt so nicht, sondern gesamte Seite
  const error = getFetchConfigFailure(state) || getModifyConfigFailure(state);
  const config = getConfig(state);
  const configUpdatePermission = getConfigUpdatePermission(state);

  return {
    loading,
    error,
    config,
    configUpdatePermission
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("config")(GlobalConfig));
