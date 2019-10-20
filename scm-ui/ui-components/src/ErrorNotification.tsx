import React from "react";
import { translate } from "react-i18next";
import { BackendError, ForbiddenError, UnauthorizedError } from "./errors";
import Notification from "./Notification";
import BackendErrorNotification from "./BackendErrorNotification";

type Props = {
  t: (p: string) => string;
  error?: Error;
};

class ErrorNotification extends React.Component<Props> {
  render() {
    const { t, error } = this.props;
    if (error) {
      if (error instanceof BackendError) {
        return <BackendErrorNotification error={error} />;
      } else if (error instanceof UnauthorizedError) {
        return (
          <Notification type="danger">
            <strong>{t("errorNotification.prefix")}:</strong>{" "}
            {t("errorNotification.timeout")}{" "}
            <a href="javascript:window.location.reload(true)">
              {t("errorNotification.loginLink")}
            </a>
          </Notification>
        );
      } else if (error instanceof ForbiddenError) {
        return (
          <Notification type="danger">
            <strong>{t("errorNotification.prefix")}:</strong>{" "}
            {t("errorNotification.forbidden")}
          </Notification>
        );
      } else {
        return (
          <Notification type="danger">
            <strong>{t("errorNotification.prefix")}:</strong> {error.message}
          </Notification>
        );
      }
    }
    return null;
  }
}

export default translate("commons")(ErrorNotification);
