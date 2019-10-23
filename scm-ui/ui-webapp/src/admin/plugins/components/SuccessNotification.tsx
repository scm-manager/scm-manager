import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Notification } from "@scm-manager/ui-components";

class InstallSuccessNotification extends React.Component<WithTranslation> {
  render() {
    const { t } = this.props;
    return (
      <Notification type="success">
        {t("plugins.modal.successNotification")}{" "}
        <a onClick={e => window.location.reload(true)}>{t("plugins.modal.reload")}</a>
      </Notification>
    );
  }
}

export default withTranslation("admin")(InstallSuccessNotification);
