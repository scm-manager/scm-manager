//@flow
import React from "react";
import { translate } from "react-i18next";
import Notification from "./Notification";

type Props = {
  t: string => string,
  error?: Error
};

class ErrorNotification extends React.Component<Props> {
  render() {
    const { t, error } = this.props;
    if (error) {
      return (
        <Notification type="danger">
          <strong>{t("error-notification.prefix")}:</strong> {error.message}
        </Notification>
      );
    }
    return "";
  }
}

export default translate("commons")(ErrorNotification);
