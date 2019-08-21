// @flow
import * as React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { compose } from "redux";
import type { PluginCollection } from "@scm-manager/ui-types";
import {
  Loading,
  Title,
  Subtitle,
  Notification,
  ErrorNotification,
  Button
} from "@scm-manager/ui-components";
import {
  fetchPluginsByLink,
  getFetchPluginsFailure,
  getPluginCollection,
  isFetchPluginsPending
} from "../modules/plugins";
import PluginsList from "../components/PluginList";
import {
  getAvailablePluginsLink,
  getInstalledPluginsLink
} from "../../../modules/indexResource";
import PluginTopActions from "../components/PluginTopActions";
import PluginBottomActions from "../components/PluginBottomActions";
import InstallPendingAction from '../components/InstallPendingAction';

type Props = {
  loading: boolean,
  error: Error,
  collection: PluginCollection,
  baseUrl: string,
  installed: boolean,
  availablePluginsLink: string,
  installedPluginsLink: string,

  // context objects
  t: string => string,

  // dispatched functions
  fetchPluginsByLink: (link: string) => void
};

class PluginsOverview extends React.Component<Props> {
  componentDidMount() {
    const {
      installed,
      fetchPluginsByLink,
      availablePluginsLink,
      installedPluginsLink
    } = this.props;
    fetchPluginsByLink(installed ? installedPluginsLink : availablePluginsLink);
  }

  componentDidUpdate(prevProps) {
    const {
      installed,
      fetchPluginsByLink,
      availablePluginsLink,
      installedPluginsLink
    } = this.props;
    if (prevProps.installed !== installed) {
      fetchPluginsByLink(
        installed ? installedPluginsLink : availablePluginsLink
      );
    }
  }

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
    const { collection } = this.props;
    if (collection._links.installPending) {
      return (
        <InstallPendingAction collection={collection} />
      );
    }
    return null;
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
      </>
    );
  }

  renderPluginsList() {
    const { collection, t } = this.props;

    if (collection._embedded && collection._embedded.plugins.length > 0) {
      return <PluginsList plugins={collection._embedded.plugins} />;
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

  return {
    collection,
    loading,
    error,
    availablePluginsLink,
    installedPluginsLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchPluginsByLink: (link: string) => {
      dispatch(fetchPluginsByLink(link));
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
