// @flow
import React from "react";
import { Button } from "@scm-manager/ui-components";
import type {PendingPlugins, PluginCollection} from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import MultiPluginActionModal from "./MultiPluginActionModal";

export const MultiPluginActionType = {
  UPDATE_ALL: "updateAll",
  CANCEL_PENDING: "cancelPending",
  EXECUTE_PENDING: "executePending"
};

type Props = {
  actionType: string,
  pendingPlugins?: PendingPlugins,
  installedPlugins?: PluginCollection,

  // context props
  t: string => string
};

type State = {
  showModal: boolean
};

class MultiPluginAction extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      showModal: false
    };
  }

  toggleModal = () => {
    this.setState(state => ({
      showModal: !state.showModal
    }));
  };

  renderLabel = () => {
    const {t, actionType} = this.props;
    if (actionType === MultiPluginActionType.EXECUTE_PENDING) {
      return t("plugins.executePending");
    } else if (actionType === MultiPluginActionType.CANCEL_PENDING) {
      return t("plugins.cancelPending");
    } else {
      return t("plugins.updateAll");
    }
  };

  renderModal = () => {
    const { showModal } = this.state;
    const {pendingPlugins, installedPlugins, actionType} = this.props;
    if (showModal) {
      return (
        <MultiPluginActionModal
          pendingPlugins={
            actionType === MultiPluginActionType.UPDATE_ALL
              ? null
              : pendingPlugins
          }
          installedPlugins={
            actionType !== MultiPluginActionType.UPDATE_ALL
              ? null
              : installedPlugins
          }
          onClose={this.toggleModal}
          actionType={actionType}
        />
      );
    }
    return null;
  };

  render() {
    return (
      <>
        {this.renderModal()}
        <Button
          color="primary"
          label={this.renderLabel()}
          action={this.toggleModal}
        />
      </>
    );
  }
}

export default translate("admin")(MultiPluginAction);
