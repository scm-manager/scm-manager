// @flow
import React from "react";
import { Button } from "@scm-manager/ui-components";
import type { PendingPlugins, PluginCollection } from "@scm-manager/ui-types";
import { translate } from "react-i18next";

export const MultiPluginActionType = {
  UPDATE_ALL: "updateAll",
  CANCEL_PENDING: "cancelPending",
  EXECUTE_PENDING: "executePending"
};

type Props = {
  actionType: string,
  pendingPlugins?: PendingPlugins,
  installedPlugins?: PluginCollection,
  refresh: () => void,
  onClick: () => void,

  // context props
  t: (key: string, params?: Object) => string
};

class MultiPluginAction extends React.Component<Props> {

  renderLabel = () => {
    const { t, actionType, installedPlugins } = this.props;

    if (actionType === MultiPluginActionType.EXECUTE_PENDING) {
      return t("plugins.executePending");
    } else if (actionType === MultiPluginActionType.CANCEL_PENDING) {
      return t("plugins.cancelPending");
    } else {
      const outdatedPlugins = installedPlugins._embedded.plugins.filter(
        p => p._links.update
      ).length;
      return t("plugins.outdatedPlugins", {
        count: outdatedPlugins
      });
    }
  };

  renderIcon = () => {
    const { actionType } = this.props;

    if (actionType === MultiPluginActionType.EXECUTE_PENDING) {
      return "arrow-circle-right";
    } else if (actionType === MultiPluginActionType.CANCEL_PENDING) {
      return "times";
    } else {
      return "sync-alt";
    }
  };

  render() {
    const { onClick } = this.props;
    return (
      <>
        <Button
          color="primary"
          reducedMobile={true}
          icon={this.renderIcon()}
          label={this.renderLabel()}
          action={() => onClick()}
        />
      </>
    );
  }
}

export default translate("admin")(MultiPluginAction);
