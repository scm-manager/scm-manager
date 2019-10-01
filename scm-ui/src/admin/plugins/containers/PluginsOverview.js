// @flow
import * as React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { compose } from "redux";
import type { PendingPlugins, PluginCollection } from "@scm-manager/ui-types";
import {
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
  getAvailablePluginsLink,
  getInstalledPluginsLink,
  getPendingPluginsLink
} from "../../../modules/indexResource";
import PluginTopActions from "../components/PluginTopActions";
import PluginBottomActions from "../components/PluginBottomActions";
import MultiPluginAction from "../components/MultiPluginAction";
import ExecutePendingActionModal from "../components/ExecutePendingActionModal";
import CancelPendingActionModal from "../components/CancelPendingActionModal";
import UpdateAllActionModal from "../components/UpdateAllActionModal";

type Props = {
  loading: boolean,
  error: Error,
  collection: PluginCollection,
  baseUrl: string,
  installed: boolean,
  availablePluginsLink: string,
  installedPluginsLink: string,
  pendingPluginsLink: string,
  pendingPlugins: PendingPlugins,

  // context objects
  t: (key: string, params?: Object) => string,

  // dispatched functions
  fetchPluginsByLink: (link: string) => void,
  fetchPendingPlugins: (link: string) => void
};

type State = {
  showPendingModal: boolean,
  showUpdateAllModal: boolean,
  showCancelModal: boolean
}

class PluginsOverview extends React.Component<Props, State> {


  constructor(props: Props, context: *) {
    super(props, context);
    this.state = {
      showPendingModal: false,
      showUpdateAllModal: false,
      showCancelModal: false
    };
  }

  componentDidMount() {
    const {
      installed,
      fetchPluginsByLink,
      availablePluginsLink,
      installedPluginsLink,
      pendingPluginsLink,
      fetchPendingPlugins
    } = this.props;
    fetchPluginsByLink(installed ? installedPluginsLink : availablePluginsLink);
    fetchPendingPlugins(pendingPluginsLink);
  }

  componentDidUpdate(prevProps) {
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
    fetchPendingPlugins(pendingPluginsLink);
  };

  renderHeader = (actions: React.Node) => {
    const { installed, t } = this.props;
    return (
      <div className="columns">
        <div className="column">
          <Title title={t("plugins.title")} />
          <Subtitle
            subtitle={
              installed
                ? t("plugins.installedSubtitle")
                : t("plugins.availableSubtitle")
            }
          />
        </div>
        <PluginTopActions>{actions}</PluginTopActions>
      </div>
    );
  };

  renderFooter = (actions: React.Node) => {
    if (actions) {
      return <PluginBottomActions>{actions}</PluginBottomActions>;
    }
    return null;
  };

  createActions = () => {
    const { pendingPlugins, collection, t } = this.props;
    const buttons = [];

    if (
      pendingPlugins &&
      pendingPlugins._links &&
      pendingPlugins._links.execute
    ) {
      buttons.push(
        <MultiPluginAction
          key={"executePending"}
          icon={"arrow-circle-right"}
          label={t("plugins.executePending")}
          refresh={this.fetchPlugins}
          onClick={() => this.setState({showPendingModal: true})}
        />
      );
    }

    if (
      pendingPlugins &&
      pendingPlugins._links &&
      pendingPlugins._links.cancel
    ) {
      buttons.push(
        <MultiPluginAction
          key={"cancelPending"}
          icon={"times"}
          label={t("plugins.cancelPending")}
          refresh={this.fetchPlugins}
          onClick={() => this.setState({showCancelModal: true})}
        />
      );
    }

    if (collection && collection._links && collection._links.update) {
      buttons.push(
        <MultiPluginAction
          key={"updateAll"}
          icon={"sync-alt"}
          label={this.computeUpdateAllSize()}
          refresh={this.fetchPlugins}
          onClick={() => this.setState({showUpdateAllModal: true})}
        />
      );
    }

    if (buttons.length > 0) {
      return <ButtonGroup>{buttons}</ButtonGroup>;
    }
    return null;
  };

  computeUpdateAllSize = () => {
    const {collection, t} = this.props;
    const outdatedPlugins = collection._embedded.plugins.filter(
      p => p._links.update
    ).length;
    return t("plugins.outdatedPlugins", {
      count: outdatedPlugins
    });
  };

  render() {
    const { loading, error, collection, pendingPlugins } = this.props;

    const { showPendingModal, showCancelModal, showUpdateAllModal} = this.state;

    if (showPendingModal) {
      return <ExecutePendingActionModal
        onClose={() => this.setState({showPendingModal: false})}
        pendingPlugins={pendingPlugins}
      />;
    }
    if (showCancelModal) {
      return <CancelPendingActionModal
        onClose={() => this.setState({showCancelModal: false})}
        refresh={this.fetchPlugins}
        pendingPlugins={pendingPlugins}
      />;
    }
    if (showUpdateAllModal) {
      return <UpdateAllActionModal
        onClose={() => this.setState({showUpdateAllModal: false})}
        refresh={this.fetchPlugins}
        installedPlugins={collection}
      />;
    }

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
      </>
    );
  }

  renderPluginsList() {
    const { collection, t } = this.props;

    if (collection._embedded && collection._embedded.plugins.length > 0) {
      return (
        <PluginsList
          plugins={collection._embedded.plugins}
          refresh={this.fetchPlugins}
        />
      );
    }
    return <Notification type="info">{t("plugins.noPlugins")}</Notification>;
  }
}

const mapStateToProps = state => {
  const collection = getPluginCollection(state);
  const loading = isFetchPluginsPending(state);
  const error = getFetchPluginsFailure(state);
  const availablePluginsLink = getAvailablePluginsLink(state);
  const installedPluginsLink = getInstalledPluginsLink(state);
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

const mapDispatchToProps = dispatch => {
  return {
    fetchPluginsByLink: (link: string) => {
      dispatch(fetchPluginsByLink(link));
    },
    fetchPendingPlugins: (link: string) => {
      dispatch(fetchPendingPlugins(link));
    }
  };
};

export default compose(
  translate("admin"),
  connect(
    mapStateToProps,
    mapDispatchToProps
  )
)(PluginsOverview);
