//@flow
import React from "react";
import { translate } from "react-i18next";
import Notification from "./Notification";
import {BackendError, UnauthorizedError} from "./errors";

type Props = {
  t: string => string,
  error?: Error
};

class ErrorNotification extends React.Component<Props> {

  renderMoreInformationLink(error: BackendError) {
    if (error.url) {
      // TODO i18n
      return (
        <p>
          For more information, see <a href={error.url} target="_blank">{error.errorCode}</a>
        </p>
      );
    }
  }

  renderBackendError = (error: BackendError) => {
    // TODO i18n
    // how should we handle i18n for the message?
    // we could add translation for known error codes to i18n and pass the context objects as parameters,
    // but this will not work for errors from plugins, because the ErrorNotification will search for the translation
    // in the wrong namespace (plugins could only add translations to the plugins namespace.
    // should we add a special namespace for errors? which could be extend by plugins?

    // TODO error page
    // we have currently the ErrorNotification, which is often wrapped by the ErrorPage
    // the ErrorPage has often a SubTitle like "Unkwown xzy error", which is no longer always the case
    // if the error is a BackendError its not fully unknown
    return (
      <div className="content">
        <p>{error.message}</p>
        <p><strong>Context:</strong></p>
        <ul>
          {error.context.map((context, index) => {
            return (
              <li key={index}>
                <strong>{context.type}:</strong> {context.id}
              </li>
            );
          })}
        </ul>
        { this.renderMoreInformationLink(error) }
        <div className="level is-size-7">
          <div className="left">
            ErrorCode: {error.errorCode}
          </div>
          <div className="right">
            TransactionId: {error.transactionId}
          </div>
        </div>
      </div>
    );
  }

  render() {
    const { t, error } = this.props;
    if (error) {
      if (error instanceof BackendError) {
        return this.renderBackendError(error)
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
