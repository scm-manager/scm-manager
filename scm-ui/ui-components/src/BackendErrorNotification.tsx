/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { BackendError } from "./errors";
import Notification from "./Notification";

import { WithTranslation, withTranslation } from "react-i18next";

type Props = WithTranslation & {
  error: BackendError;
};

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
          {this.renderAdditionalMessages()}
          <p>{this.renderViolations()}</p>
          {this.renderMetadata()}
        </div>
      </Notification>
    );
  }

  renderErrorName = () => {
    const { error, t } = this.props;
    const translation = t(`errors.${error.errorCode}.displayName`);
    if (translation === error.errorCode) {
      return error.message;
    }
    return translation;
  };

  renderErrorDescription = () => {
    const { error, t } = this.props;
    const translation = t(`errors.${error.errorCode}.description`);
    if (translation === error.errorCode) {
      return "";
    }
    return translation;
  };

  renderAdditionalMessages = () => {
    const { error, t } = this.props;
    if (error.additionalMessages) {
      return error.additionalMessages.map(a => a.key ? t(`errors.${a.key}.description`) : a.message).map(m => <p>{m}</p>);
    }
  };

  renderViolations = () => {
    const { error, t } = this.props;
    if (error.violations) {
      return (
        <>
          <p>
            <strong>{t("errors.violations")}</strong>
          </p>
          <ul>
            {error.violations.map((violation, index) => {
              return (
                <li key={index}>
                  {violation.path && <strong>{violation.path}:</strong>} {violation.message}{" "}
                  {violation.key && t(violation.key)}
                </li>
              );
            })}
          </ul>
        </>
      );
    }
  };

  renderMetadata = () => {
    const { error, t } = this.props;
    return (
      <>
        {this.renderContext()}
        {this.renderMoreInformationLink()}
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

  renderContext = () => {
    const { error, t } = this.props;
    if (error.context) {
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
        </>
      );
    }
  };

  renderMoreInformationLink = () => {
    const { error, t } = this.props;
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

export default withTranslation("plugins")(BackendErrorNotification);
