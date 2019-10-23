import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { PluginCollection } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";
import PluginActionModal from "./PluginActionModal";

type Props = WithTranslation & {
  onClose: () => void;
  refresh: () => void;
  installedPlugins: PluginCollection;
};

class UpdateAllActionModal extends React.Component<Props> {
  render() {
    const { onClose, installedPlugins, t } = this.props;

    return (
      <PluginActionModal
        description={t("plugins.modal.updateAll")}
        label={t("plugins.updateAll")}
        onClose={onClose}
        installedPlugins={installedPlugins}
        execute={this.updateAll}
      />
    );
  }

  updateAll = () => {
    const { installedPlugins, refresh, onClose } = this.props;
    return apiClient
      .post(installedPlugins._links.update.href)
      .then(refresh)
      .then(onClose);
  };
}

export default withTranslation("admin")(UpdateAllActionModal);
