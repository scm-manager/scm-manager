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

type Props = {
  loading: boolean,
  error: Error,
  config: Config,
  configUpdatePermission: boolean,

  // dispatch functions
  modifyConfig: (config: Config, callback?: () => void) => void,
  fetchConfig: void => void,
  configReset: void => void,

  // context objects
  t: string => string
};

class GlobalConfig extends React.Component<Props> {
  componentDidMount() {
    this.props.configReset();
    this.props.fetchConfig();
  }

  modifyConfig = (config: Config) => {
    this.props.modifyConfig(config);
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
