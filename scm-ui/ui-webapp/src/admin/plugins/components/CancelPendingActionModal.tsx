import React from "react";
import PluginActionModal from "./PluginActionModal";
import { PendingPlugins } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";
import { WithTranslation, withTranslation } from "react-i18next";

type Props = WithTranslation & {
  onClose: () => void;
  refresh: () => void;
  pendingPlugins: PendingPlugins;
};

class CancelPendingActionModal extends React.Component<Props> {
  render() {
    const { onClose, pendingPlugins, t } = this.props;

    return (
      <PluginActionModal
        description={t("plugins.modal.cancelPending")}
        label={t("plugins.cancelPending")}
        onClose={onClose}
        pendingPlugins={pendingPlugins}
        execute={this.cancelPending}
      />
    );
  }

  cancelPending = () => {
    const { pendingPlugins, refresh, onClose } = this.props;
    return apiClient
      .post(pendingPlugins._links.cancel.href)
      .then(refresh)
      .then(onClose);
  };
}

export default withTranslation("admin")(CancelPendingActionModal);
