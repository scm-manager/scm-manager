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
import * as React from "react";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { compose } from "redux";
import { PendingPlugins, Plugin, PluginCollection } from "@scm-manager/ui-types";
import {
  Button,
  ButtonGroup,
  ErrorNotification,
  Loading,
  Notification,
  Subtitle,
  Title
} from "@scm-manager/ui-components";
import {
  fetchPendingPlugins,
  fetchPluginsByLink,
  getFetchPluginsFailure,
  getPendingPlugins,
  getPluginCollection,
  isFetchPluginsPending
} from "../modules/plugins";
import PluginsList from "../components/PluginList";
import {
  getPendingPluginsLink,
  mustGetAvailablePluginsLink,
  mustGetInstalledPluginsLink
} from "../../../modules/indexResource";
import PluginTopActions from "../components/PluginTopActions";
import PluginBottomActions from "../components/PluginBottomActions";
import ExecutePendingActionModal from "../components/ExecutePendingActionModal";
import CancelPendingActionModal from "../components/CancelPendingActionModal";
import UpdateAllActionModal from "../components/UpdateAllActionModal";
import ShowPendingModal from "../components/ShowPendingModal";

type Props = WithTranslation & {
  loading: boolean;
  error: Error;
  collection: PluginCollection;
  baseUrl: string;
  installed: boolean;
  availablePluginsLink: string;
  installedPluginsLink: string;
  pendingPluginsLink: string;
  pendingPlugins: PendingPlugins;

  // dispatched functions
  fetchPluginsByLink: (link: string) => void;
  fetchPendingPlugins: (link: string) => void;
};

type State = {
  showPendingModal: boolean;
  showExecutePendingModal: boolean;
  showUpdateAllModal: boolean;
  showCancelModal: boolean;
};

class PluginsOverview extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      showPendingModal: false,
      showExecutePendingModal: false,
      showUpdateAllModal: false,
      showCancelModal: false
    };
  }

  componentDidMount() {
    this.fetchPlugins();
  }

  componentDidUpdate(prevProps: Props) {
    const { installed } = this.props;
    if (prevProps.installed !== installed) {
      this.fetchPlugins();
    }
  }

  fetchPlugins = () => {
    const {
      installed,
      fetchPluginsByLink,
      availablePluginsLink,
      installedPluginsLink,
      pendingPluginsLink,
      fetchPendingPlugins
    } = this.props;
    fetchPluginsByLink(installed ? installedPluginsLink : availablePluginsLink);
    if (pendingPluginsLink) {
      fetchPendingPlugins(pendingPluginsLink);
    }
  };

  renderHeader = (actions: React.ReactNode) => {
    const { installed, t } = this.props;
    return (
      <div className="columns">
        <div className="column">
          <Title title={t("plugins.title")} />
          <Subtitle subtitle={installed ? t("plugins.installedSubtitle") : t("plugins.availableSubtitle")} />
        </div>
        <PluginTopActions>{actions}</PluginTopActions>
      </div>
    );
  };

  renderFooter = (actions: React.ReactNode) => {
    if (actions) {
      return <PluginBottomActions>{actions}</PluginBottomActions>;
    }
    return null;
  };

  createActions = () => {
    const { pendingPlugins, collection, t } = this.props;
    const buttons = [];

    if (pendingPlugins && pendingPlugins._links) {
      if (pendingPlugins._links.execute) {
        buttons.push(
          <Button
            color="primary"
            reducedMobile={true}
            key={"executePending"}
            icon={"arrow-circle-right"}
            label={t("plugins.executePending")}
            action={() =>
              this.setState({
                showExecutePendingModal: true
              })
            }
          />
        );
      }

      if (pendingPlugins._links.cancel) {
        if (!pendingPlugins._links.execute) {
          buttons.push(
            <Button
              color="primary"
              reducedMobile={true}
              key={"showPending"}
              icon={"info"}
              label={t("plugins.showPending")}
              action={() =>
                this.setState({
                  showPendingModal: true
                })
              }
            />
          );
        }

        buttons.push(
          <Button
            color="primary"
            reducedMobile={true}
            key={"cancelPending"}
            icon={"times"}
            label={t("plugins.cancelPending")}
            action={() =>
              this.setState({
                showCancelModal: true
              })
            }
          />
        );
      }
    }

    if (collection && collection._links && collection._links.update) {
      buttons.push(
        <Button
          color="primary"
          reducedMobile={true}
          key={"updateAll"}
          icon={"sync-alt"}
          label={this.computeUpdateAllSize()}
          action={() =>
            this.setState({
              showUpdateAllModal: true
            })
          }
        />
      );
    }

    if (buttons.length > 0) {
      return <ButtonGroup>{buttons}</ButtonGroup>;
    }
    return null;
  };

  computeUpdateAllSize = () => {
    const { collection, t } = this.props;
    const outdatedPlugins = collection._embedded.plugins.filter((p: Plugin) => p._links.update).length;
    return t("plugins.outdatedPlugins", {
      count: outdatedPlugins
    });
  };

  render() {
    const { loading, error, collection } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (!collection || loading) {
      return <Loading />;
    }

    const actions = this.createActions();
    return (
      <>
        {this.renderHeader(actions)}
        <hr className="header-with-actions" />
        {this.renderPluginsList()}
        {this.renderFooter(actions)}
        {this.renderModals()}
      </>
    );
  }

  renderModals = () => {
    const { collection, pendingPlugins } = this.props;
    const { showPendingModal, showExecutePendingModal, showCancelModal, showUpdateAllModal } = this.state;

    if (showPendingModal) {
      return (
        <ShowPendingModal
          onClose={() =>
            this.setState({
              showPendingModal: false
            })
          }
          pendingPlugins={pendingPlugins}
        />
      );
    }

    if (showExecutePendingModal) {
      return (
        <ExecutePendingActionModal
          onClose={() =>
            this.setState({
              showExecutePendingModal: false
            })
          }
          pendingPlugins={pendingPlugins}
        />
      );
    }
    if (showCancelModal) {
      return (
        <CancelPendingActionModal
          onClose={() =>
            this.setState({
              showCancelModal: false
            })
          }
          refresh={this.fetchPlugins}
          pendingPlugins={pendingPlugins}
        />
      );
    }
    if (showUpdateAllModal) {
      return (
        <UpdateAllActionModal
          onClose={() =>
            this.setState({
              showUpdateAllModal: false
            })
          }
          refresh={this.fetchPlugins}
          installedPlugins={collection}
        />
      );
    }
  };

  renderPluginsList() {
    const { collection, t } = this.props;

    if (collection._embedded && collection._embedded.plugins.length > 0) {
      return <PluginsList plugins={collection._embedded.plugins} refresh={this.fetchPlugins} />;
    }
    return <Notification type="info">{t("plugins.noPlugins")}</Notification>;
  }
}

const mapStateToProps = (state: any) => {
  const collection = getPluginCollection(state);
  const loading = isFetchPluginsPending(state);
  const error = getFetchPluginsFailure(state);
  const availablePluginsLink = mustGetAvailablePluginsLink(state);
  const installedPluginsLink = mustGetInstalledPluginsLink(state);
  const pendingPluginsLink = getPendingPluginsLink(state);
  const pendingPlugins = getPendingPlugins(state);

  return {
    collection,
    loading,
    error,
    availablePluginsLink,
    installedPluginsLink,
    pendingPluginsLink,
    pendingPlugins
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchPluginsByLink: (link: string) => {
      dispatch(fetchPluginsByLink(link));
    },
    fetchPendingPlugins: (link: string) => {
      dispatch(fetchPendingPlugins(link));
    }
  };
};

export default compose(withTranslation("admin"), connect(mapStateToProps, mapDispatchToProps))(PluginsOverview);
