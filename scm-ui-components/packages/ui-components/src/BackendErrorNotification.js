// @flow
import React from "react";
import { BackendError } from "./errors";
import Notification from "./Notification";

import { translate } from "react-i18next";

type Props = { error: BackendError, t: string => string };

class BackendErrorNotification extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
  }

  render() {
    return (
      <Notification type="danger">
        <div className="content">
          <p className="subtitle">{this.renderErrorName()}</p>
          <p>{this.renderErrorDescription()}</p>
          {this.renderMetadata()}
        </div>
      </Notification>
    );
  }

  renderErrorName = () => {
    const { error, t } = this.props;
    const translation = t("errors." + error.errorCode + ".displayName");
    if (translation === error.errorCode) {
      return error.message;
    }
    return translation;
  };

  renderErrorDescription = () => {
    const { error, t } = this.props;
    const translation = t("errors." + error.errorCode + ".description");
    if (translation === error.errorCode) {
      return "";
    }
    return translation;
  };

  renderMetadata = () => {
    const { error, t } = this.props;
    return (
      <>
        <p>
          <strong>{t("errors.context")}</strong>
        </p>
        <ul>
          {error.context.map((context, index) => {
            return (
              <li key={index}>
                <strong>{context.type}:</strong> {context.id}
              </li>
            );
          })}
        </ul>
        {this.renderMoreInformationLink(error)}
        <div className="level is-size-7">
          <div className="left">
            {t("errors.transactionId")} {error.transactionId}
          </div>
          <div className="right">
            {t("errors.errorCode")} {error.errorCode}
          </div>
        </div>
      </>
    );
  };

  renderMoreInformationLink = (error: BackendError) => {
    const { t } = this.props;
    if (error.url) {
      return (
        <p>
          {t("errors.moreInfo")}{" "}
          <a href={error.url} target="_blank">
            {error.errorCode}
          </a>
        </p>
      );
    }
  };
}

export default translate("plugins")(BackendErrorNotification);
