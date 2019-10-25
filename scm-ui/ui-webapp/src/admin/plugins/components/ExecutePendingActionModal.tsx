import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { PendingPlugins } from "@scm-manager/ui-types";
import { apiClient, Notification } from "@scm-manager/ui-components";
import waitForRestart from "./waitForRestart";
import PluginActionModal from "./PluginActionModal";

type Props = WithTranslation & {
  onClose: () => void;
  pendingPlugins: PendingPlugins;
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
        <Notification type="warning">{t("plugins.modal.restartNotification")}</Notification>
      </PluginActionModal>
    );
  }

  executeAndRestart = () => {
    const { pendingPlugins } = this.props;
    return apiClient.post(pendingPlugins._links.execute.href).then(waitForRestart);
  };
}

export default withTranslation("admin")(ExecutePendingActionModal);
