import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Notification } from "@scm-manager/ui-components";
import { PluginAction } from "./PluginEntry";

type Props = WithTranslation & {
  pluginAction?: string;
};

class InstallSuccessNotification extends React.Component<Props> {
  createMessageForPluginAction = () => {
    const { pluginAction, t } = this.props;
    if (pluginAction === PluginAction.INSTALL) {
      return t("plugins.modal.installedNotification");
    } else if (pluginAction === PluginAction.UPDATE) {
      return t("plugins.modal.updatedNotification");
    } else if (pluginAction === PluginAction.UNINSTALL) {
      return t("plugins.modal.uninstalledNotification");
    }
    return t("plugins.modal.executedChangesNotification");
  };

  render() {
    const { t } = this.props;
    return (
      <Notification type="success">
        {this.createMessageForPluginAction()}{" "}
        <a onClick={e => window.location.reload(true)}>{t("plugins.modal.reload")}</a>
      </Notification>
    );
  }
}

export default withTranslation("admin")(InstallSuccessNotification);
