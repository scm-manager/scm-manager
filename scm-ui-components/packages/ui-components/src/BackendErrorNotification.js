// @flow
import React from "react";
import { BackendError } from "./errors";
import classNames from "classnames";

import { translate } from "react-i18next";

type Props = { error: BackendError, t: string => string };
type State = { collapsed: boolean };

class BackendErrorNotification extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { collapsed: true };
  }

  render() {
    const { collapsed } = this.state;
    const icon = collapsed ? "fa-angle-right" : "fa-angle-down";

    // TODO error page
    // we have currently the ErrorNotification, which is often wrapped by the ErrorPage
    // the ErrorPage has often a SubTitle like "Unkwown xzy error", which is no longer always the case
    // if the error is a BackendError its not fully unknown
    return (
      <div className="content">
        <p>
          <span
            onClick={() => {
              this.setState({ collapsed: !this.state.collapsed });
            }}
          >
            <i className={classNames("fa", icon)} />

            {this.renderErrorMessage()}
          </span>
        </p>
        {this.renderUncollapsed()}
      </div>
    );
  }

  renderErrorMessage = () => {
    const { error, t } = this.props;
    const translation = t("errors." + error.errorCode);
    if (translation === error.errorCode) {
      return error.message;
    }
    return translation;
  };

  renderUncollapsed = () => {
    const { error } = this.props;
    if (!this.state.collapsed) {
      return (
        <>
          <p>
            <strong>Context:</strong>
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
            <div className="left">ErrorCode: {error.errorCode}</div>
            <div className="right">TransactionId: {error.transactionId}</div>
          </div>
        </>
      );
    }
    return null;
  };

  renderMoreInformationLink = (error: BackendError) => {
    if (error.url) {
      // TODO i18n
      return (
        <p>
          For more information, see{" "}
          <a href={error.url} target="_blank">
            {error.errorCode}
          </a>
        </p>
      );
    }
  };
}

export default translate("plugins")(BackendErrorNotification);
