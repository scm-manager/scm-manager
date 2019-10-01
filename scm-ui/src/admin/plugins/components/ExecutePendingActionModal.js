// @flow

import React from "react";
import PluginActionModal from "./PluginActionModal";
import type { PendingPlugins } from "@scm-manager/ui-types";
import waitForRestart from "./waitForRestart";
import { apiClient, Notification } from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type Props = {
  onClose: () => void,
  pendingPlugins: PendingPlugins,

  // context props
  t: string => string
};

class ExecutePendingActionModal extends React.Component<Props> {
  render() {
    const { onClose, pendingPlugins, t } = this.props;

    return (
      <PluginActionModal
        description={t("plugins.modal.executePending")}
        label={t("plugins.modal.executeAndRestart")}
        onClose={onClose}
        pendingPlugins={pendingPlugins}
        execute={this.executeAndRestart}
      >
        <Notification type="warning">
          {t("plugins.modal.restartNotification")}
        </Notification>
      </PluginActionModal>
    );
  }

  executeAndRestart = () => {
    const { pendingPlugins } = this.props;
    return apiClient
      .post(pendingPlugins._links.execute.href)
      .then(waitForRestart);
  };
}

export default translate("admin")(ExecutePendingActionModal);
