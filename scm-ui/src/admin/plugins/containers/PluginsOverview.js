// @flow
import React from "react";
import { translate } from "react-i18next";
import { Loading, Title, Subtitle, LinkPaginator, Notification } from "@scm-manager/ui-components";
import PluginsList from "../components/PluginsList";

type Props = {
  loading: boolean,
  error: Error,
  baseUrl: string,
  installed: boolean,

  // context objects
  t: string => string
};

class PluginsOverview extends React.Component<Props> {
  render() {
    const { loading, installed, t } = this.props;

    if (loading) {
      return <Loading />;
    }

    return (
      <>
        <Title title={t("plugins.title")} />
        <Subtitle
          subtitle={
            installed
              ? t("plugins.installedSubtitle")
              : t("plugins.availableSubtitle")
          }
        />
        {this.renderPluginsList()}
      </>
    );
  }

  renderPluginsList() {
    const { collection, page, t } = this.props;

    if (collection._embedded && collection._embedded.plugins.length > 0) {
      return (
        <>
          <PluginsList plugins={collection._embedded.plugins} />
          <LinkPaginator collection={collection} page={page} />
        </>
      );
    }
    return (
      <Notification type="info">{t("plugins.noPlugins")}</Notification>
    );
  }
}

export default translate("admin")(PluginsOverview);
