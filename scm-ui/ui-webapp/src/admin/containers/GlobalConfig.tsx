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
import { connect } from "react-redux";
import { Config, NamespaceStrategies } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, Title } from "@scm-manager/ui-components";
import {getConfigLink, mustGetConfigLink} from "../../modules/indexResource";
import {
  fetchConfig,
  getConfig,
  getConfigUpdatePermission,
  getFetchConfigFailure,
  getModifyConfigFailure,
  isFetchConfigPending,
  isModifyConfigPending,
  modifyConfig,
  modifyConfigReset
} from "../modules/config";
import ConfigForm from "../components/form/ConfigForm";
import {
  fetchNamespaceStrategiesIfNeeded,
  getFetchNamespaceStrategiesFailure,
  getNamespaceStrategies,
  isFetchNamespaceStrategiesPending
} from "../modules/namespaceStrategies";
import { compose } from "redux";

type Props = WithTranslation & {
  loading: boolean;
  error: Error;
  config: Config;
  configUpdatePermission: boolean;
  configLink: string;
  namespaceStrategies?: NamespaceStrategies;

  // dispatch functions
  modifyConfig: (config: Config, callback?: () => void) => void;
  fetchConfig: (link: string) => void;
  configReset: (p: void) => void;
  fetchNamespaceStrategiesIfNeeded: (p: void) => void;
};

type State = {
  configReadPermission: boolean;
  configChanged: boolean;
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
      this.setState({
        configReadPermission: false
      });
    }
  }

  modifyConfig = (config: Config) => {
    this.props.modifyConfig(config);
    this.setState({
      configChanged: true
    });
  };

  renderConfigChangedNotification = () => {
    if (this.state.configChanged) {
      return (
        <div className="notification is-primary">
          <button
            className="delete"
            onClick={() =>
              this.setState({
                configChanged: false
              })
            }
          />
          {this.props.t("config.form.submit-success-notification")}
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

const mapDispatchToProps = (dispatch: any) => {
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

const mapStateToProps = (state: any) => {
  const loading =
    isFetchConfigPending(state) || isModifyConfigPending(state) || isFetchNamespaceStrategiesPending(state);
  const error =
    getFetchConfigFailure(state) || getModifyConfigFailure(state) || getFetchNamespaceStrategiesFailure(state);

  const config = getConfig(state);
  const configUpdatePermission = getConfigUpdatePermission(state);
  const configLink = mustGetConfigLink(state);
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

export default compose(connect(mapStateToProps, mapDispatchToProps), withTranslation("config"))(GlobalConfig);
