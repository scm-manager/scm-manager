//@flow
import React from "react";
import { translate } from "react-i18next";
import Notification from "./Notification";
import { UNAUTHORIZED_ERROR } from "./apiclient";

type Props = {
  t: string => string,
  error?: Error
};

class ErrorNotification extends React.Component<Props> {
  render() {
    const { t, error } = this.props;
    if (error) {
      if (error == UNAUTHORIZED_ERROR) {
        return (
          <Notification type="danger">
            <strong>{t("error-notification.prefix")}:</strong> {t("error-notification.timeout")} <Link to={"/login"}>Login</Link>
          </Notification>
        );
      } else {
        return (
          <Notification type="danger">
            <strong>{t("error-notification.prefix")}:</strong> {error.message}
          </Notification>
        );
      }
    }
    return null;
  }
}

export default translate("commons")(ErrorNotification);
